import { createRootRouteWithContext, Outlet } from "@tanstack/react-router";
import NavBar from "@/features/navigation/components/NavBar.tsx";
import Footer from "@/features/navigation/components/Footer.tsx";
import type { RouterContext } from "@/shared/types/router-types.ts";

export const Route = createRootRouteWithContext<RouterContext>()({
    component: () => (
        <div>
            <NavBar />
            <Outlet />
            <Footer />
        </div>
    ),
});
