import { useApp } from "../state";
import { Page } from "../components/ui";
import { Calibration, TestCards } from "./Home";

export default function Tests() {
  const { t } = useApp();
  return (
    <Page title={t("nav_tests")} subtitle={t("disclaimer")}>
      <div className="grid gap-6 lg:grid-cols-[1fr_22rem]">
        <TestCards only={["quick", "full", "arrangement", "contrast"]} />
        <Calibration />
      </div>
    </Page>
  );
}
