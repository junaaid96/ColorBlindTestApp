//! Colour-vision maths shared with the frontend (`src/lib/color.ts`).
//! Machado, Oliveira & Fernandes (2009) matrices at severity 1.0, applied in linear sRGB.

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Cvd {
    Protan,
    Deutan,
    Tritan,
}

impl Cvd {
    pub fn parse(s: &str) -> Option<Self> {
        match s {
            "protan" => Some(Self::Protan),
            "deutan" => Some(Self::Deutan),
            "tritan" => Some(Self::Tritan),
            _ => None,
        }
    }
    fn machado(self) -> Mat3 {
        match self {
            Cvd::Protan => [
                0.152286, 1.052583, -0.204868, 0.114503, 0.786281, 0.099216, -0.003882, -0.048116,
                1.051998,
            ],
            Cvd::Deutan => [
                0.367322, 0.860646, -0.227968, 0.280085, 0.672501, 0.047413, -0.011820, 0.042940,
                0.968881,
            ],
            Cvd::Tritan => [
                1.255528, -0.076749, -0.178779, -0.078411, 0.930809, 0.147602, 0.004733, 0.691367,
                0.303900,
            ],
        }
    }
}

pub type Mat3 = [f32; 9];
const IDENTITY: Mat3 = [1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0];

fn lerp(a: &Mat3, b: &Mat3, t: f32) -> Mat3 {
    std::array::from_fn(|i| a[i] + (b[i] - a[i]) * t)
}
fn mul(a: &Mat3, b: &Mat3) -> Mat3 {
    std::array::from_fn(|i| {
        let (r, c) = (i / 3, i % 3);
        (0..3).map(|k| a[r * 3 + k] * b[k * 3 + c]).sum()
    })
}

/// Simulation matrix interpolated from identity by severity (0..1).
pub fn simulation(t: Cvd, severity: f32) -> Mat3 {
    lerp(&IDENTITY, &t.machado(), severity.clamp(0.0, 1.0))
}

/// Fidaner-style daltonization: I + K (I - S), shifting lost information to visible channels.
pub fn correction(t: Cvd, severity: f32) -> Mat3 {
    let s = simulation(t, severity);
    let k: Mat3 = match t {
        Cvd::Tritan => [0.7, 0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0],
        _ => [0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.7, 0.0, 1.0],
    };
    let diff: Mat3 = std::array::from_fn(|i| IDENTITY[i] - s[i]);
    let kd = mul(&k, &diff);
    std::array::from_fn(|i| IDENTITY[i] + kd[i])
}

fn lut_to_linear() -> [f32; 256] {
    std::array::from_fn(|i| {
        let c = i as f32 / 255.0;
        if c <= 0.04045 {
            c / 12.92
        } else {
            ((c + 0.055) / 1.055).powf(2.4)
        }
    })
}
fn to_gamma_u8(c: f32) -> u8 {
    let c = c.clamp(0.0, 1.0);
    let g = if c <= 0.003_130_8 {
        c * 12.92
    } else {
        1.055 * c.powf(1.0 / 2.4) - 0.055
    };
    (g * 255.0).round() as u8
}

/// Applies a linear-RGB matrix to RGBA8 pixels in place (alpha untouched).
pub fn apply(pixels: &mut [u8], m: &Mat3) {
    let lut = lut_to_linear();
    for px in pixels.chunks_exact_mut(4) {
        let (r, g, b) = (
            lut[px[0] as usize],
            lut[px[1] as usize],
            lut[px[2] as usize],
        );
        px[0] = to_gamma_u8(m[0] * r + m[1] * g + m[2] * b);
        px[1] = to_gamma_u8(m[3] * r + m[4] * g + m[5] * b);
        px[2] = to_gamma_u8(m[6] * r + m[7] * g + m[8] * b);
    }
}

/// Parses "protan-sim" / "deutan-fix" etc. into a matrix.
pub fn matrix_for_mode(mode: &str, severity: f32) -> Result<Mat3, String> {
    let (ty, op) = mode
        .split_once('-')
        .ok_or("mode must be <type>-<sim|fix>")?;
    let t = Cvd::parse(ty).ok_or_else(|| format!("unknown type {ty}"))?;
    match op {
        "sim" => Ok(simulation(t, severity)),
        "fix" => Ok(correction(t, severity)),
        _ => Err(format!("unknown op {op}")),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn rows_of_simulation_sum_to_one() {
        for t in [Cvd::Protan, Cvd::Deutan, Cvd::Tritan] {
            let m = simulation(t, 1.0);
            for r in 0..3 {
                let s: f32 = m[r * 3..r * 3 + 3].iter().sum();
                assert!((s - 1.0).abs() < 1e-3, "{t:?} row {r} sums to {s}");
            }
        }
    }

    #[test]
    fn zero_severity_is_identity() {
        assert_eq!(simulation(Cvd::Deutan, 0.0), IDENTITY);
        let mut px = [12, 200, 77, 255, 0, 0, 0, 0];
        let before = px;
        apply(&mut px, &simulation(Cvd::Protan, 0.0));
        assert_eq!(px, before);
    }

    #[test]
    fn greys_are_preserved_and_alpha_untouched() {
        let mut px = [128, 128, 128, 42];
        apply(&mut px, &simulation(Cvd::Tritan, 1.0));
        for c in &px[..3] {
            assert!((*c as i32 - 128).abs() <= 1);
        }
        assert_eq!(px[3], 42);
    }

    #[test]
    fn protan_red_and_green_converge() {
        let mut red = [255, 0, 0, 255];
        let mut green = [0, 160, 0, 255];
        let m = simulation(Cvd::Protan, 1.0);
        apply(&mut red, &m);
        apply(&mut green, &m);
        let dist = |a: &[u8], b: &[u8]| {
            (0..3)
                .map(|i| (a[i] as f32 - b[i] as f32).powi(2))
                .sum::<f32>()
                .sqrt()
        };
        assert!(dist(&red, &green) < dist(&[255, 0, 0], &[0, 160, 0]) * 0.6);
    }

    #[test]
    fn modes_parse() {
        assert!(matrix_for_mode("deutan-fix", 1.0).is_ok());
        assert!(matrix_for_mode("protan", 1.0).is_err());
        assert!(matrix_for_mode("blue-sim", 1.0).is_err());
        assert!(matrix_for_mode("tritan-zap", 1.0).is_err());
    }

    #[test]
    fn correction_identity_at_zero_severity() {
        let m = correction(Cvd::Deutan, 0.0);
        for (a, b) in m.iter().zip(IDENTITY.iter()) {
            assert!((a - b).abs() < 1e-6);
        }
    }
}
