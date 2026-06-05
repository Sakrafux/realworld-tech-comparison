import type { ComponentChildren } from "preact";
import { useEffect, useState } from "preact/hooks";
import { isAuthenticated } from "@/util/auth-util.ts";
import { navigate } from "astro:transitions/client";

export type AuthGuardProps = {
    children: ComponentChildren;
};

export default function AuthGuard({ children }: AuthGuardProps) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        if (isAuthenticated()) {
            setIsLoggedIn(true);
        } else {
            navigate("/login");
        }
    }, []);

    if (!isLoggedIn) {
        return null;
    }

    return children;
}
