param()

$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env.local"

if (-not (Test-Path $envFile)) {
  Write-Error "Arquivo local nao encontrado: $envFile"
  Write-Host "Use o .env.local.example como base para criar o seu .env.local."
  exit 1
}

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
