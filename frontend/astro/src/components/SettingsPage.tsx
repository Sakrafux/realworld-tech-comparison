import { login, logout } from "@/util/auth-util.ts";
import { navigate } from "astro:transitions/client";
import type { TargetedSubmitEvent } from "preact";
import { useEffect, useState } from "preact/hooks";
import { getUser, updateUser } from "@/api/features/user-api.ts";

export default function SettingsPage() {
    const [image, setImage] = useState("");
    const [username, setUsername] = useState("");
    const [bio, setBio] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState<string>();

    useEffect(() => {
        getUser().then(({ user }) => {
            setImage(user.image || "");
            setUsername(user.username || "");
            setBio(user.bio || "");
            setEmail(user.email || "");
        });
    }, []);

    const handleSubmit = async (e: TargetedSubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(undefined);

        try {
            const result = await updateUser({
                user: {
                    email: email,
                    password: password || undefined,
                    username: username,
                    bio: bio,
                    image: image,
                },
            });
            login(result.user);
            navigate(`/profile/${result.user.username}`);
        } catch (err) {
            setError((err as Error).message || "Error at user update");
        }
    };

    return (
        <div className="settings-page">
            <div className="container page">
                <div className="row">
                    <div className="col-md-6 offset-md-3 col-xs-12">
                        <h1 className="text-xs-center">Your Settings</h1>

                        {error && (
                            <ul className="error-messages">
                                <li>{error}</li>
                            </ul>
                        )}

                        <form onSubmit={handleSubmit}>
                            <fieldset>
                                <fieldset className="form-group">
                                    <input
                                        className="form-control"
                                        type="text"
                                        name="image"
                                        placeholder="URL of profile picture"
                                        value={image}
                                        onChange={(e) => setImage(e.currentTarget.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <input
                                        className="form-control form-control-lg"
                                        type="text"
                                        name="username"
                                        placeholder="Your Name"
                                        value={username}
                                        onChange={(e) => setUsername(e.currentTarget.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <textarea
                                        className="form-control form-control-lg"
                                        name="bio"
                                        rows={8}
                                        placeholder="Short bio about you"
                                        value={bio}
                                        onChange={(e) => setBio(e.currentTarget.value)}
                                    ></textarea>
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
                                        placeholder="New Password"
                                        value={password}
                                        onChange={(e) => setPassword(e.currentTarget.value)}
                                    />
                                </fieldset>
                                <button
                                    className="btn btn-lg btn-primary pull-xs-right"
                                    type="submit"
                                >
                                    Update Settings
                                </button>
                            </fieldset>
                        </form>
                        <hr />
                        <button
                            class="btn btn-outline-danger"
                            onClick={() => {
                                logout();
                                navigate("/");
                            }}
                        >
                            Or click here to logout.
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
