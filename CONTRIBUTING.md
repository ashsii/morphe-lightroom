# Contributing

These are personal patches for Lightroom Android 9.3.0, version code
`710903001`, on `arm64-v8a`. Include the package name, version, architecture,
selected patches, and reproduction steps when reporting a problem. Redact
tokens, account details, and photo paths from logs. Do not attach APKs or
private signing keys.

## Development

1. Branch from `dev` and keep changes focused.
2. Follow the [build setup](README.md#build), using JDK 21 and user-level
   GitHub Packages credentials.
3. Run `./gradlew buildAndroid --console=plain` (or `./gradlew.bat` on Windows).
4. For patch changes, apply the bundle to the supported APK and verify the
   modified methods or manifest. Test affected behavior on a device when
   possible, and state clearly which checks were performed.
5. Use conventional commits: `feat:` for features, `fix:` for fixes, and
   `chore:` for maintenance. Open pull requests against `dev`.

Do not commit APKs, downloaded tools, decompiled app sources, credentials,
private signing material, or build output. Keep the upstream license and notice.

## Release setup

The inherited GitHub Actions workflows may need enabling in a newly created
fork. In repository Settings > Actions > General, allow GitHub Actions to
create pull requests if using the automatic `dev`-to-`main` pull request.
The workflow token must be able to read the Morphe registry's packages.

Releases run through the existing [release workflow](.github/workflows/release.yml)
and [semantic-release configuration](.releaserc). Push changes to `dev` for
prereleases; merge `dev` into `main` without squashing for stable releases.
Do not force-push release commits or manually edit generated release metadata.

Optional signing: run `./gradlew generateSigningKey`, commit only
`morphe-signing.pub`, and store the private key as the repository Actions secret
`MORPHE_SIGNING_KEY`. Back up the private key securely outside the checkout.
