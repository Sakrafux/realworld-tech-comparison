import { useState, type SubmitEvent } from "react";
import { Link, useNavigate } from "@tanstack/react-router";
import { login as loginWithApi } from "@/features/auth/api/login-api.ts";
import { useAuth } from "@/features/auth/context/auth-context.tsx";

export default function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string>();

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(undefined);

        if (!email || !password) {
            setError("Please fill in all fields");
            return;
        }

        try {
            const result = await loginWithApi({ user: { email, password } });
            login(result.user.token);
        } catch (err) {
            setError((err as Error).message || "Invalid credentials");
        }

        await navigate({ to: "/" });
    };

    return (
        <div className="auth-page">
            <div className="container page">
                <div className="row">
                    <div className="col-md-6 offset-md-3 col-xs-12">
                        <h1 className="text-xs-center">Sign in</h1>
                        <p className="text-xs-center">
                            <Link to="/register">Need an account?</Link>
                        </p>

                        {error && (
                            <ul className="error-messages">
                                <li>{error}</li>
                            </ul>
                        )}

                        <form onSubmit={handleSubmit}>
                            <fieldset className="form-group">
                                <input
                                    className="form-control form-control-lg"
                                    type="email"
                                    placeholder="Email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                />
                            </fieldset>
                            <fieldset className="form-group">
                                <input
                                    className="form-control form-control-lg"
                                    type="password"
                                    placeholder="Password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                />
                            </fieldset>
                            <button type="submit" className="btn btn-lg btn-primary pull-xs-right">
                                Sign in
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}
