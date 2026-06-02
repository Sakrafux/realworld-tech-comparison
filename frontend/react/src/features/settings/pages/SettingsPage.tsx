import { useState, useEffect, type SubmitEvent } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { getUser, updateUser } from "@/shared/api/features/user-api.ts";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { useNavigate } from "@tanstack/react-router";

// Authentication is assumed
export default function SettingsPage() {
    const { data: userData, isLoading } = useQuery({
        queryKey: ["user"],
        queryFn: () => getUser(),
    });

    const user = userData?.user;

    const [image, setImage] = useState("");
    const [username, setUsername] = useState("");
    const [bio, setBio] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState<string>();

    const { login, logout } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    useEffect(() => {
        if (user) {
            setImage(user.image || "");
            setUsername(user.username || "");
            setBio(user.bio || "");
            setEmail(user.email || "");
        }
    }, [user]);

    if (isLoading) {
        return null;
    }

    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(undefined);

        try {
            const result = await updateUser({
                user: {
                    email: email || undefined,
                    password: password || undefined,
                    username: username || undefined,
                    bio: bio || undefined,
                    image: image || undefined,
                },
            });
            login(result.user);

            queryClient.setQueryData(["user"], () => result);

            await navigate({ to: `/profile/${result.user.username}` });
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
                                        onChange={(e) => setImage(e.target.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <input
                                        className="form-control form-control-lg"
                                        type="text"
                                        name="username"
                                        placeholder="Your Name"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <textarea
                                        className="form-control form-control-lg"
                                        name="bio"
                                        rows={8}
                                        placeholder="Short bio about you"
                                        value={bio}
                                        onChange={(e) => setBio(e.target.value)}
                                    ></textarea>
                                </fieldset>
                                <fieldset className="form-group">
                                    <input
                                        className="form-control form-control-lg"
                                        type="email"
                                        name="email"
                                        placeholder="Email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                    />
                                </fieldset>
                                <fieldset className="form-group">
                                    <input
                                        className="form-control form-control-lg"
                                        type="password"
                                        name="password"
                                        placeholder="New Password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
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
                            type="button"
                            className="btn btn-outline-danger"
                            onClick={async () => {
                                logout();
                                await navigate({ to: "/" });
                            }}
                        >
                            Or click here to logout
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
