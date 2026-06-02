import { Link } from "@tanstack/react-router";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { useQuery } from "@tanstack/react-query";
import { getUser } from "@/shared/api/features/user-api.ts";

export default function NavBar() {
    const { isAuthenticated } = useAuth();

    const { data, isSuccess } = useQuery({
        queryKey: ["user"],
        queryFn: () => getUser(),
        enabled: isAuthenticated,
    });

    return (
        <nav className="navbar navbar-light">
            <div className="container">
                <Link className="navbar-brand" to="/">
                    conduit
                </Link>
                <ul className="nav navbar-nav pull-xs-right">
                    <li className="nav-item">
                        <Link className="nav-link" to="/">
                            Home
                        </Link>
                    </li>
                    {isAuthenticated && isSuccess ? (
                        <>
                            <li className="nav-item">
                                <Link className="nav-link" to="/editor">
                                    <i className="ion-compose"></i>&nbsp;New Article
                                </Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link" to="/settings">
                                    <i className="ion-gear-a"></i>&nbsp;Settings
                                </Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link" to={`/profile/${data.user.username}`}>
                                    <img
                                        src={data.user.image || "./default-avatar.svg"}
                                        alt="avatar"
                                        className="user-pic"
                                    />
                                    {data.user.username}
                                </Link>
                            </li>
                        </>
                    ) : (
                        <>
                            <li className="nav-item">
                                <Link className="nav-link" to="/login">
                                    Sign in
                                </Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link" to="/register">
                                    Sign up
                                </Link>
                            </li>
                        </>
                    )}
                </ul>
            </div>
        </nav>
    );
}
