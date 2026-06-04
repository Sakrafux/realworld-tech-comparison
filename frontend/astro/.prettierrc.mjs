// .prettierrc.mjs
/** @type {import("prettier").Config} */
export default {
    tabWidth: 4,
    useTabs: false,
    bracketSpacing: true,
    singleQuote: false,
    trailingComma: "all",
    semi: true,
    plugins: ["prettier-plugin-astro"],
    overrides: [
        {
            files: "*.astro",
            options: {
                parser: "astro",
            },
        },
    ],
};
