import { useState } from "preact/hooks";
import type { TargetedSubmitEvent } from "preact";
import { navigate } from "astro:transitions/client";
import { login } from "@/util/auth-util.ts";
import { login as loginWithApi } from "@/api/features/user-api.ts";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string>();

    const handleSubmit = async (e: TargetedSubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(undefined);

        if (!email || !password) {
            setError("Please fill in all fields");
            return;
        }

        try {
            const result = await loginWithApi({ user: { email, password } });
            login(result.user);
            navigate("/");
        } catch (err) {
            setError((err as Error).message || "Invalid credentials");
        }
    };

    return (
        <div class="auth-page">
            <div class="container page">
                <div class="row">
                    <div class="col-md-6 offset-md-3 col-xs-12">
                        <h1 class="text-xs-center">Sign in</h1>
                        <p class="text-xs-center">
                            <a href="/register">Need an account?</a>
                        </p>

                        {error && (
                            <ul class="error-messages">
                                <li>{error}</li>
                            </ul>
                        )}

                        <form onSubmit={handleSubmit}>
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
                                Sign in
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
}
