# PowerShell script to download test dependencies

$libDir = "lib"
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir
}

# Download JUnit 5
$junitVersion = "5.9.3"
$junitPlatformVersion = "1.9.3"

Write-Host "Downloading JUnit 5 dependencies..."

# JUnit Jupiter API
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/$junitVersion/junit-jupiter-api-$junitVersion.jar" -OutFile "$libDir/junit-jupiter-api-$junitVersion.jar"

# JUnit Jupiter Engine
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-engine/$junitVersion/junit-jupiter-engine-$junitVersion.jar" -OutFile "$libDir/junit-jupiter-engine-$junitVersion.jar"

# JUnit Platform Commons
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-commons/$junitPlatformVersion/junit-platform-commons-$junitPlatformVersion.jar" -OutFile "$libDir/junit-platform-commons-$junitPlatformVersion.jar"

# JUnit Platform Engine
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-engine/$junitPlatformVersion/junit-platform-engine-$junitPlatformVersion.jar" -OutFile "$libDir/junit-platform-engine-$junitPlatformVersion.jar"

# JUnit Platform Launcher
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-launcher/$junitPlatformVersion/junit-platform-launcher-$junitPlatformVersion.jar" -OutFile "$libDir/junit-platform-launcher-$junitPlatformVersion.jar"

# Download jqwik
$jqwikVersion = "1.7.4"
Write-Host "Downloading jqwik..."
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/net/jqwik/jqwik/$jqwikVersion/jqwik-$jqwikVersion.jar" -OutFile "$libDir/jqwik-$jqwikVersion.jar"

# Download opentest4j (required by JUnit)
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar" -OutFile "$libDir/opentest4j-1.2.0.jar"

# Download apiguardian (required by JUnit)
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar" -OutFile "$libDir/apiguardian-api-1.1.2.jar"

# Download servlet API
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar" -OutFile "$libDir/javax.servlet-api-4.0.1.jar"

Write-Host "All dependencies downloaded successfully!"
