import { useEffect, useState } from "preact/hooks";
import { getClassNameForCurrentPath } from "@/util/active-path-util.ts";
import { getCurrentUser, isAuthenticated } from "@/util/auth-util.ts";

export type NavBarProps = {
    initialPath: string;
};

export default function NavBar({ initialPath }: NavBarProps) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    // Control the active path via component state
    const [currentPath, setCurrentPath] = useState(initialPath);

    useEffect(() => {
        setIsLoggedIn(isAuthenticated());

        // Listen for Astro page changes
        const handlePageChange = () => setCurrentPath(window.location.pathname);
        document.addEventListener("astro:after-swap", handlePageChange);
        return () => document.removeEventListener("astro:after-swap", handlePageChange);
    }, []);

    return (
        <nav class="navbar navbar-light">
            <div class="container">
                <a class="navbar-brand" href="/">
                    conduit
                </a>
                <ul class="nav navbar-nav pull-xs-right">
                    <li class="nav-item">
                        <a
                            class={getClassNameForCurrentPath("nav-link", "/", currentPath)}
                            href="/"
                        >
                            Home
                        </a>
                    </li>
                    {isLoggedIn ? (
                        <AuthenticatedLinks currentPath={currentPath} />
                    ) : (
                        <UnauthenticatedLinks currentPath={currentPath} />
                    )}
                </ul>
            </div>
        </nav>
    );
}

function UnauthenticatedLinks({ currentPath }: { currentPath: string }) {
    return (
        <>
            <li class="nav-item">
                <a
                    class={getClassNameForCurrentPath("nav-link", "/login", currentPath)}
                    href="/login"
                >
                    Sign in
                </a>
            </li>
            <li class="nav-item">
                <a
                    class={getClassNameForCurrentPath("nav-link", "/register", currentPath)}
                    href="/register"
                >
                    Sign up
                </a>
            </li>
        </>
    );
}

function AuthenticatedLinks({ currentPath }: { currentPath: string }) {
    // At this point, it is established we are both on the client *and* authenticated
    const user = getCurrentUser()!;

    return (
        <>
            <li key="editor" class="nav-item">
                <a
                    class={getClassNameForCurrentPath("nav-link", "/editor", currentPath)}
                    href="/editor"
                >
                    <i class="ion-compose"></i>&nbsp;New Article
                </a>
            </li>
            <li class="nav-item">
                <a
                    class={getClassNameForCurrentPath("nav-link", "/settings", currentPath)}
                    href="/settings"
                >
                    <i class="ion-gear-a"></i>&nbsp;Settings
                </a>
            </li>
            <li class="nav-item">
                <a
                    class={getClassNameForCurrentPath(
                        "nav-link",
                        `/profile/${user.username}`,
                        currentPath,
                    )}
                    href={`/profile/${user.username}`}
                >
                    <img alt="avatar" src={user.image || "/default-avatar.svg"} class="user-pic" />
                    {user.username}
                </a>
            </li>
        </>
    );
}
