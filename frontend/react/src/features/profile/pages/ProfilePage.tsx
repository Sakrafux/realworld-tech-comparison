import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
    followUserByUsername,
    getProfile,
    type ProfileResponse,
    unfollowUserByUsername,
} from "@/shared/api/features/profile-api.ts";
import { useAuth } from "@/features/auth/context/auth-context.tsx";
import { Link, useLocation, useNavigate, useParams, useSearch } from "@tanstack/react-router";
import { useMemo } from "react";
import ArticlePreview from "@/features/article/components/ArticlePreview.tsx";
import { getArticles } from "@/shared/api/features/article-api.ts";

const PAGE_SIZE = 5;

export default function ProfilePage() {
    const { username } = useParams({ strict: false }) as { username: string };
    const location = useLocation();
    const isFavoritesRoute = location.pathname.endsWith("/favorites");
    const search = useSearch({ strict: false }) as { page?: number };
    const currentPage = search.page ?? 1;

    const { username: currentUsername, isAuthenticated } = useAuth();

    const isCurrentUser = username === currentUsername;

    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { data: profile, isLoading } = useQuery({
        queryKey: ["profile", username],
        queryFn: () => getProfile(username),
    });

    const { data: articles } = useQuery({
        queryKey: ["articles", username, isFavoritesRoute, currentPage],
        queryFn: () =>
            getArticles({
                favorited: isFavoritesRoute ? username : undefined,
                author: !isFavoritesRoute ? username : undefined,
                offset: (currentPage - 1) * PAGE_SIZE,
                limit: PAGE_SIZE,
            }),
        initialData: () => ({ articles: [], articlesCount: 0 }),
    });

    const pageElements = useMemo(() => {
        const elements = [];
        for (let i = 1; i <= Math.ceil(articles.articlesCount / PAGE_SIZE); i++) {
            elements.push(
                <li key={`page-link-${i}`} className="page-item">
                    <Link className="page-link" to="." search={{ page: i }}>
                        {i}
                    </Link>
                </li>,
            );
        }
        return elements;
    }, [articles.articlesCount, currentPage]);

    const articleElements = useMemo(() => {
        return articles.articles.map((article) => (
            <ArticlePreview
                key={article.slug}
                article={article}
                queryKey={["articles", username, isFavoritesRoute, currentPage]}
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
                                src={profile.profile.image || "./default-avatar.svg"}
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
                                        to="/profile/$username"
                                        params={{ username }}
                                        activeProps={{
                                            className: !isFavoritesRoute ? "active" : "",
                                        }}
                                    >
                                        My Articles
                                    </Link>
                                </li>
                                <li className="nav-item">
                                    <Link
                                        className="nav-link"
                                        to="/profile/$username/favorites"
                                        params={{ username }}
                                        activeProps={{
                                            className: isFavoritesRoute ? "active" : "",
                                        }}
                                    >
                                        Favorited
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
