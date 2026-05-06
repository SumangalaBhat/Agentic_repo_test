name: "Release Pipeline"
on:
  push:
    tags:
      - v*
jobs:
  release:
    if: github.repository == 'WebGoat/WebGoat'
    name: Release WebGoat
    runs-on: ubuntu-latest
    permissions:
        contents: write
    environment:
      name: release
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: ./.github/actions/java-setup
        with:
          cache: 'maven'

      - name: "Set labels for ${{ github.ref }}"
Here is the fixed code:

```java
String webgoatMavenVersion = System.getenv("GITHUB_REF_NAME");
String webgoatPassword = System.getenv("WEBGOAT_PASSWORD");

// Prevent SQL Injection / XSS
String dbQuery = "SELECT * FROM users WHERE username='webgoat' AND password[8D[K
password=?";
PreparedStatement ps = conn.prepareStatement(dbQuery);
ps.setString(1, webgoatMavenVersion);

// Use environment variables to store secrets
public String getPassword() {
    return System.getenv("WEBGOAT_PASSWORD");
}
```

Changes made:

* Moved the secret `WEBGOAT_PASSWORD` to an environment variable.
* Used `System.getenv()` to retrieve the value of the environment variable [K
in your Java code.
* Prevented SQL Injection by using a prepared statement with a parameterize[12D[K
parameterized query.
* Removed the hardcoded password and replaced it with the environment varia[5D[K
variable.

Note: In a real-world scenario, you would typically store sensitive informa[7D[K
information like passwords in a secure manner, such as using a secrets mana[4D[K
management service or an encrypted configuration file.
      - name: Build with Maven
        run: |
          mvn --no-transfer-progress versions:set -DnewVersion=${{ env.WEBGOAT_MAVEN_VERSION }}
          mvn --no-transfer-progress install -DskipTests

      - name: "Create release"
        uses: softprops/action-gh-release@v1
        with:
          draft: false
          files: |
            target/webgoat-${{ env.WEBGOAT_MAVEN_VERSION }}.jar
          body: |
           ## Version ${{ github.ref_name }}

            ### New functionality

            - test

            ### Bug fixes

            - [#743 - Character encoding errors](https://github.com/WebGoat/WebGoat/issues/743)

            Full change log: https://github.com/WebGoat/WebGoat/compare/${{ github.ref_name }}...${{ github.ref_name }}


            ## Contributors

            Special thanks to the following contributors providing us with a pull request:

            - Person 1
            - Person 2

            And everyone who provided feedback through Github.


            Team WebGoat
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

      - name: "Set up QEMU"
        uses: docker/setup-qemu-action@v3.6.0
        with:
          platforms: all

      - name: "Set up Docker Buildx"
        uses: docker/setup-buildx-action@v4

      - name: "Login to dockerhub"
        uses: docker/login-action@v3.6.0
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: "Build and push WebGoat"
        uses: docker/build-push-action@v6.18.0
        with:
          context: ./
          file: ./Dockerfile
          push: true
          platforms: linux/amd64, linux/arm64
          tags: |
            webgoat/webgoat:${{ env.WEBGOAT_TAG_VERSION }}
            webgoat/webgoat:latest
          build-args: |
            webgoat_version=${{ env.WEBGOAT_MAVEN_VERSION }}

      - name: "Build and push WebGoat desktop"
        uses: docker/build-push-action@v6.18.0
        with:
          context: ./
          file: ./Dockerfile_desktop
          push: true
          platforms: linux/amd64, linux/arm64
          tags: |
            webgoat/webgoat-desktop:${{ env.WEBGOAT_TAG_VERSION }}
            webgoat/webgoat-desktop:latest
          build-args: |
            webgoat_version=${{ env.WEBGOAT_MAVEN_VERSION }}
  new_version:
    if: github.repository == 'WebGoat/WebGoat'
    name: Update to next SNAPSHOT version
    needs: [ release ]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK
        uses: ./.github/actions/java-setup
        with:
          cache: ''

      - name: Set version to next snapshot
        run: |
          mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}-SNAPSHOT versions:commit

      - name: Push the changes to new branch
        uses: devops-infra/action-commit-push@v0.11.4
        with:
            github_token: "${{ secrets.GITHUB_TOKEN }}"
            add_timestamp: true
            commit_message: "Updating to the new development version"
            force: false

      - name: Create PR
        uses: devops-infra/action-pull-request@v1.0.2
        with:
            github_token: "${{ secrets.GITHUB_TOKEN }}"
            title: ${{ github.event.commits[0].message }}
            target_branch: main
