import {
    followUserByUsername,
    type Profile,
    unfollowUserByUsername,
} from "@/api/features/profile-api.ts";
import { getCurrentUser, isAuthenticated } from "@/util/auth-util.ts";
import { navigate } from "astro:transitions/client";
import { useState } from "preact/hooks";

export type ProfileActionsProps = {
    profile: Profile;
};

export default function ProfileActions({ profile }: ProfileActionsProps) {
    const [profileState, setProfileState] = useState(profile);

    const user = getCurrentUser();

    if (user?.username === profileState.username) {
        return (
            <button
                class="btn btn-sm btn-outline-secondary action-btn"
                onClick={() => navigate("/settings")}
            >
                <i class="ion-gear-a"></i>
                &nbsp; Edit Profile Settings
            </button>
        );
    }

    if (profileState.following) {
        return (
            <button
                class="btn btn-sm btn-outline-secondary action-btn"
                onClick={() => {
                    if (!isAuthenticated()) {
                        navigate("/login");
                        return;
                    }
                    unfollowUserByUsername(profileState.username);
                    setProfileState({ ...profileState, following: false });
                }}
            >
                <i class="ion-plus-round"></i>
                &nbsp; Unfollow {profileState.username}
            </button>
        );
    }

    return (
        <button
            class="btn btn-sm btn-outline-secondary action-btn"
            onClick={() => {
                if (!isAuthenticated()) {
                    navigate("/login");
                    return;
                }
                followUserByUsername(profileState.username);
                setProfileState({ ...profileState, following: true });
            }}
        >
            <i class="ion-plus-round"></i>
            &nbsp; Follow {profileState.username}
        </button>
    );
}
