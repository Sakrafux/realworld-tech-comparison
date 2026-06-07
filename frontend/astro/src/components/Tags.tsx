import { getTags } from "@/api/features/tag-api.ts";
import { useEffect, useState } from "preact/hooks";

export default function Tags() {
    const [tags, setTags] = useState<string[]>([]);

    useEffect(() => {
        getTags().then((tags) => {
            setTags(tags.tags);
        });
    }, []);

    return (
        <div class="tag-list">
            {tags.map((tag) => (
                <a href={`/tag/${tag}`} class="tag-pill tag-default">
                    {tag}
                </a>
            ))}
        </div>
    );
}
