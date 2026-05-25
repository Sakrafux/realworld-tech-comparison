import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { getTags } from "@/shared/api/features/tag-api.ts";
import { Link } from "@tanstack/react-router";
import type { HomeSearch } from "@/routes";

export default function Tags() {
    const { data: tags } = useQuery({
        queryKey: ["tags"],
        queryFn: () => getTags(),
    });

    const tagElements = useMemo(() => {
        if (!tags) {
            return [];
        }
        return tags.tags.map((tag) => (
            <Link key={tag} to="." search={{ tag } as HomeSearch} className="tag-pill tag-default">
                {tag}
            </Link>
        ));
    }, [tags]);

    return (
        <div className="sidebar">
            <p>Popular Tags</p>

            <div className="tag-list">{tagElements}</div>
        </div>
    );
}
