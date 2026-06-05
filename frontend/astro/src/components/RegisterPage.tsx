import { useState } from "preact/hooks";
import type { TargetedSubmitEvent } from "preact";
import { register } from "@/api/features/user-api.ts";
import { login } from "@/util/auth-util.ts";
import { navigate } from "astro:transitions/client";

export default function RegisterPage() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string>();

    const handleSubmit = async (e: TargetedSubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(undefined);

        if (!username || !email || !password) {
            setError("All fields are required");
            return;
        }

        try {
            const result = await register({ user: { username, email, password } });
            login(result.user);
            navigate("/");
        } catch (err) {
            setError((err as Error).message || "Something went wrong during registration");
        }
    };

    return (
        <div className="auth-page">
            <div className="container page">
                <div className="row">
                    <div className="col-md-6 offset-md-3 col-xs-12">
                        <h1 className="text-xs-center">Sign up</h1>
                        <p className="text-xs-center">
                            <a href="/login">Have an account?</a>
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
                                    type="text"
                                    name="username"
                                    placeholder="Username"
                                    value={username}
                                    onChange={(e) => setUsername(e.currentTarget.value)}
                                />
                            </fieldset>
                            <fieldset className="form-group">
                                <input
                                    className="form-control form-control-lg"
                                    type="email"
                                    name="email"
                                    placeholder="Email"
                                    value={email}
                                    onChange={(e) => setEmail(e.currentTarget.value)}
                                />
                            </fieldset>
                            <fieldset className="form-group">
                                <input
                                    className="form-control form-control-lg"
                                    type="password"
                                    name="password"
                                    placeholder="Password"
                                    value={password}
                                    onChange={(e) => setPassword(e.currentTarget.value)}
                                />
                            </fieldset>
                            <button type="submit" className="btn btn-lg btn-primary pull-xs-right">
                                Sign up
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}
