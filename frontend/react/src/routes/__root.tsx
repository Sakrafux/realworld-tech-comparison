import { createRootRouteWithContext, Outlet } from "@tanstack/react-router";
import type { RouterContext } from "@/shared/types/router-types.ts";
import NavBar from "@/components/NavBar.tsx";
import Footer from "@/components/Footer.tsx";

export const Route = createRootRouteWithContext<RouterContext>()({
    component: () => (
        <div>
            <NavBar />
            <Outlet />
            <Footer />
        </div>
    ),
});
