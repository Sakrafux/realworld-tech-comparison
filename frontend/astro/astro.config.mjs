// @ts-check
import { defineConfig } from "astro/config";
import preact from "@astrojs/preact";

import node from "@astrojs/node";

// https://astro.build/config
export default defineConfig({
  server: { port: 3000 },
  integrations: [preact()],

  adapter: node({
    mode: "standalone",
  }),
});