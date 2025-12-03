# Voiid Countdown Timer Docs

Source for the Mintlify-powered documentation site that covers installation, configuration, and API usage for both the datapack and plugin.

## Layout
- `index.mdx`: Landing page for the documentation site.
- `docs.json`: Mintlify configuration that controls the navigation, metadata, and SEO.
- `get-started/` and `usage-guide/`: User-facing guides for installing and configuring VCT.
- `api-docs/`: API references and endpoint descriptions.
- `changelogs/`: Release notes grouped by version.
- `images/` and `favicon.png`: Assets used throughout the docs.

## Developing
Use the Mintlify CLI to preview the site locally:

```bash
npm install -g mintlify
mintlify dev
```

The docs assume the repository layout above, so navigation points directly to the `datapack/` and `plugin/` source folders.
