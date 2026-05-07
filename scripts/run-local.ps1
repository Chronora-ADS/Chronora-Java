param()

$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env.local"

if (-not (Test-Path $envFile)) {
  Write-Error "Arquivo local nao encontrado: $envFile"
  Write-Host "Use o .env.local.example como base para criar o seu .env.local."
  exit 1
}

function Get-EnvFileValues {
  param(
    [Parameter(Mandatory = $true)]
    [string] $Path
  )

  $values = @{}

  Get-Content $Path |
    Where-Object {
      $line = $_.Trim()
      $line.Length -gt 0 -and -not $line.StartsWith('#')
    } |
    ForEach-Object {
      $parts = $_ -split '=', 2
      if ($parts.Count -eq 2) {
        $key = $parts[0].Trim()
        $value = $parts[1].Trim()
        $values[$key] = $value
      }
    }

  return $values
}

function Stop-BackendOnPortIfOwnedByProject {
  param(
    [Parameter(Mandatory = $true)]
    [int] $Port,

    [Parameter(Mandatory = $true)]
    [string] $ProjectRoot
  )

  $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
  if (-not $connections) {
    return
  }

  foreach ($connection in $connections) {
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($connection.OwningProcess)" -ErrorAction SilentlyContinue
    if (-not $process) {
      continue
    }

    $commandLine = [string]$process.CommandLine
    $isProjectProcess = $commandLine -like "*$ProjectRoot*" -or
      $commandLine -like '*client-server*' -or
      $commandLine -like '*spring-boot*'

    if (-not $isProjectProcess) {
      Write-Error "A porta $Port ja esta em uso pelo processo $($process.ProcessId): $commandLine"
      Write-Host "Defina outra porta na variavel PORT ou encerre esse processo manualmente."
      exit 1
    }

    Write-Host "Encerrando instancia antiga do backend na porta $Port (PID $($process.ProcessId))."
    Stop-Process -Id $process.ProcessId -Force -ErrorAction Stop
  }

  $deadline = (Get-Date).AddSeconds(15)
  do {
    Start-Sleep -Milliseconds 500
    $portStillBusy = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
  } while ($portStillBusy -and (Get-Date) -lt $deadline)

  if ($portStillBusy) {
    Write-Error "A porta $Port nao foi liberada a tempo."
    exit 1
  }
}

$envValues = Get-EnvFileValues -Path $envFile
$envValues.GetEnumerator() | ForEach-Object {
  [System.Environment]::SetEnvironmentVariable($_.Key, $_.Value, 'Process')
}

$configuredPort = $env:PORT
if ([string]::IsNullOrWhiteSpace($configuredPort)) {
  $configuredPort = '8085'
}

[int]$port = $configuredPort
Stop-BackendOnPortIfOwnedByProject -Port $port -ProjectRoot $root

& (Join-Path $root "mvnw.cmd") spring-boot:run
