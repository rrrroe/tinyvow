# Tiny Vow Release Process

This project uses one shared release version for both store flavors.

## Version Source

- Edit `TINYVOW_VERSION_NAME` and `TINYVOW_VERSION_CODE` in `gradle.properties`.
- `TINYVOW_VERSION_NAME` must use SemVer: `MAJOR.MINOR.PATCH`.
- `TINYVOW_VERSION_CODE` must be a positive integer and must increase before every external APK/AAB release.
- Do not put channel suffixes in `TINYVOW_VERSION_NAME`. The `china` flavor appends `-cn`; `googlePlay` uses the base version.

## Channel Outputs

- Google Play package: `com.rrrrz.tinyvow`.
- China package: `com.rrrrz.tinyvow.cn`.
- Google Play release upload target: `:app:bundleGooglePlayRelease`.
- Day-to-day local debug target: `:app:assembleDefaultDebug`, currently mapped to `chinaDebug`.
- Suggested artifact name: `tinyvow-{channel}-{versionName}-vc{versionCode}-{buildType}.{apk|aab}`.

## Release Checklist

1. Decide the release version and update `gradle.properties`.
2. Add a matching entry to `CHANGELOG.md`.
3. Run unit tests:

   ```powershell
   .\gradlew.bat testChinaDebugUnitTest
   ```

4. Run the default debug build:

   ```powershell
   .\gradlew.bat assembleDefaultDebug
   ```

5. Install the default debug build for a local smoke check:

   ```powershell
   .\gradlew.bat installDefaultDebug
   ```

6. For Google Play release validation, build the release bundle:

   ```powershell
   .\gradlew.bat :app:bundleGooglePlayRelease
   ```

7. For China release validation, build the China release APK:

   ```powershell
   .\gradlew.bat :app:assembleChinaRelease
   ```

8. In the app, open the Me screen and verify the localized version row:
   - China build: `1.0.0-cn`, build `1`, China channel.
   - Google Play build: `1.0.0`, build `1`, Google Play channel.

## Git Tags

- China release tag: `china-v{versionName}+{versionCode}`.
- Google Play release tag: `googleplay-v{versionName}+{versionCode}`.
- If both channels ship from the same commit, create both tags on that commit.
