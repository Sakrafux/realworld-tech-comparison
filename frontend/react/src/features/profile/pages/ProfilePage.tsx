import { type ProfileSearch, Route } from "@/routes/profile.$username.tsx";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
    followUserByUsername,
    getProfile,
    type ProfileResponse,
    unfollowUserByUsername,
} from "@/features/profile/api/profile-api.ts";
import defaultAvatar from "@/shared/assets/default-avatar.svg";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { Link, useNavigate } from "@tanstack/react-router";
import { useMemo } from "react";
import ArticlePreview from "@/features/article/components/ArticlePreview.tsx";
import { getArticles } from "@/features/article/api/article-api.ts";

const PAGE_SIZE = 5;

export default function ProfilePage() {
    const search = Route.useSearch() as ProfileSearch;
    const { username } = Route.useParams() as { username: string };

    const { username: currentUsername, isAuthenticated } = useAuth();

    const isCurrentUser = username === currentUsername;

    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { data: profile, isLoading } = useQuery({
        queryKey: ["profile", username],
        queryFn: () => getProfile(username),
    });

    const { data: articles } = useQuery({
        queryKey: ["articles", username, search.tab, search.page],
        queryFn: () =>
            getArticles({
                favorited: search.tab === "favorites" ? username : undefined,
                author: search.tab !== "favorites" ? username : undefined,
                offset: ((search.page ?? 1) - 1) * PAGE_SIZE,
                limit: PAGE_SIZE,
            }),
        initialData: () => ({ articles: [], articlesCount: 0 }),
    });

    const pageElements = useMemo(() => {
        const elements = [];
        for (let i = 1; i <= Math.ceil(articles.articlesCount / PAGE_SIZE); i++) {
            elements.push(
                <li key={`page-link-${i}`} className="page-item">
                    <Link
                        className="page-link"
                        to="."
                        search={(prev: ProfileSearch) => ({ ...prev, page: i }) as ProfileSearch}
                    >
                        {i}
                    </Link>
                </li>,
            );
        }
        return elements;
    }, [articles.articlesCount, search.page]);

    const articleElements = useMemo(() => {
        return articles.articles.map((article) => (
            <ArticlePreview
                key={article.slug}
                article={article}
                queryKey={["articles", username, search.tab, search.page]}
            />
        ));
    }, [articles.articles]);

    if (isLoading || !profile) {
        return null;
    }

    return (
        <div className="profile-page">
            <div className="user-info">
                <div className="container">
                    <div className="row">
                        <div className="col-xs-12 col-md-10 offset-md-1">
                            <img
                                src={profile.profile.image ?? defaultAvatar}
                                alt="avatar"
                                className="user-img"
                            />
                            <h4>{profile.profile.username}</h4>
                            <p>{profile.profile.bio}</p>
                            {isCurrentUser ? (
                                <Link
                                    to="/settings"
                                    className="btn btn-sm btn-outline-secondary action-btn"
                                >
                                    <i className="ion-gear-a"></i>
                                    &nbsp; Edit Profile Settings
                                </Link>
                            ) : (
                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-secondary action-btn"
                                    onClick={async () => {
                                        if (!isAuthenticated) {
                                            await navigate({ to: "/login" });
                                            return;
                                        }

                                        let following = false;
                                        if (profile.profile.following) {
                                            await unfollowUserByUsername(username);
                                        } else {
                                            await followUserByUsername(username);
                                            following = true;
                                        }

                                        queryClient.setQueryData(
                                            ["profile", username],
                                            (oldData: ProfileResponse) => {
                                                return {
                                                    profile: {
                                                        ...oldData.profile,
                                                        following,
                                                    },
                                                };
                                            },
                                        );
                                    }}
                                >
                                    {profile.profile.following ? (
                                        <>
                                            <i className="ion-minus-round"></i>
                                            &nbsp; Unfollow {username}
                                        </>
                                    ) : (
                                        <>
                                            <i className="ion-plus-round"></i>
                                            &nbsp; Follow {username}
                                        </>
                                    )}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <div className="container">
                <div className="row">
                    <div className="col-xs-12 col-md-10 offset-md-1">
                        <div className="articles-toggle">
                            <ul className="nav nav-pills outline-active">
                                <li className="nav-item">
                                    <Link
                                        className="nav-link"
                                        to="."
                                        activeProps={{
                                            className: search.tab === undefined ? "active" : "",
                                        }}
                                    >
                                        My Articles
                                    </Link>
                                </li>
                                <li className="nav-item">
                                    <Link
                                        className="nav-link"
                                        to="."
                                        search={{ tab: "favorites" } as ProfileSearch}
                                    >
                                        Favorited Articles
                                    </Link>
                                </li>
                            </ul>
                        </div>

                        {articleElements}

                        <ul className="pagination">{pageElements}</ul>
                    </div>
                </div>
            </div>
        </div>
    );
}
