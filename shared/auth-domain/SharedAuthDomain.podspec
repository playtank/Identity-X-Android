Pod::Spec.new do |spec|
    spec.name                     = 'SharedAuthDomain'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'https://github.com/your-repo/Identity-X'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Identity-X Auth Domain KMP Module'
    spec.vendored_frameworks      = 'build/cocoapods/framework/SharedAuthDomain.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target    = '15.0'
                
                
    if !Dir.exist?('build/cocoapods/framework/SharedAuthDomain.framework') || Dir.empty?('build/cocoapods/framework/SharedAuthDomain.framework')
        raise "

        Kotlin framework 'SharedAuthDomain' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :shared:auth-domain:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':shared:auth-domain',
        'PRODUCT_MODULE_NAME' => 'SharedAuthDomain',
    }
                
    spec.script_phases = [
        {
            :name => 'Build SharedAuthDomain',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev

                # ── Portable Java discovery ───────────────────────────────────
                # Xcode script phases run with a stripped PATH that omits
                # /usr/local/bin, /opt/homebrew/bin, etc.  We need to locate
                # a JDK before invoking Gradle.  Resolution order:
                #   1. JAVA_HOME already set in environment (CI / Xcode scheme var)
                #   2. /usr/libexec/java_home  (macOS-registered JDKs, e.g. Oracle)
                #   3. Homebrew openjdk (Apple Silicon and Intel paths)
                #   4. SDKMAN default
                #   5. asdf Java shim
                if [ -z "$JAVA_HOME" ]; then
                  if /usr/libexec/java_home &>/dev/null; then
                    export JAVA_HOME=$(/usr/libexec/java_home)
                  elif [ -d "/opt/homebrew/opt/openjdk@21" ]; then
                    export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
                  elif [ -d "/opt/homebrew/opt/openjdk@17" ]; then
                    export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
                  elif [ -d "/opt/homebrew/opt/openjdk" ]; then
                    export JAVA_HOME="/opt/homebrew/opt/openjdk"
                  elif [ -d "/usr/local/opt/openjdk@17" ]; then
                    export JAVA_HOME="/usr/local/opt/openjdk@17"
                  elif [ -d "$HOME/.sdkman/candidates/java/current" ]; then
                    export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
                  elif [ -f "$HOME/.asdf/plugins/java/bin/get-java-home" ]; then
                    export JAVA_HOME="$("$HOME/.asdf/plugins/java/bin/get-java-home")"
                  else
                    echo "error: Could not locate a Java runtime. Set JAVA_HOME in your Xcode scheme (Product → Scheme → Edit Scheme → Run → Environment Variables) or install a JDK via Homebrew: brew install openjdk@17"
                    exit 1
                  fi
                fi
                export PATH="$JAVA_HOME/bin:$PATH"
                echo "Using JAVA_HOME=$JAVA_HOME ($(java -version 2>&1 | head -1))"
                # ─────────────────────────────────────────────────────────────

                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end