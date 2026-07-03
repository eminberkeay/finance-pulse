"use client";

import { useSyncExternalStore } from "react";
import { alertStore } from "@/lib/alertStore";

export type { PriceAlert } from "@/lib/alertStore";

export function useAlerts() {
  const alerts = useSyncExternalStore(
    alertStore.subscribe,
    alertStore.getSnapshot,
    alertStore.getServerSnapshot,
  );

  return {
    alerts,
    addAlert: alertStore.add,
    removeAlert: alertStore.remove,
    evaluate: alertStore.evaluate,
  };
}
