param()

$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env.local"

function Set-Java17Environment {
  $javaHomeCandidates = @()

  if ($env:JAVA_HOME) {
    $javaHomeCandidates += $env:JAVA_HOME
  }

  $javaHomeCandidates += @(
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot",
    "C:\Program Files\Eclipse Adoptium\jdk-17",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-17.0.18"
  )

  $javaHome = $javaHomeCandidates |
    Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\javac.exe")) } |
    Select-Object -First 1

  if (-not $javaHome) {
    throw "Java 17 JDK nao encontrado. Instale um JDK 17 e tente novamente."
  }

  $env:JAVA_HOME = $javaHome
  $env:Path = "$javaHome\bin;$env:Path"
}

if (-not (Test-Path $envFile)) {
  Write-Error "Arquivo local nao encontrado: $envFile"
  Write-Host "Use o .env.local.example como base para criar o seu .env.local."
  exit 1
}

Set-Java17Environment

Get-Content $envFile |
  Where-Object {
    $line = $_.Trim()
    $line.Length -gt 0 -and -not $line.StartsWith('#')
  } |
  ForEach-Object {
    $parts = $_ -split '=', 2
    if ($parts.Count -eq 2) {
      $key = $parts[0].Trim()
      $value = $parts[1].Trim()
      [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
    }
  }

& (Join-Path $root "mvnw.cmd") spring-boot:run
