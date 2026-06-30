# Fase 1 - Reestructuracion de Arquitectura VetExpert
# Script seguro sin caracteres especiales en strings

$BASE = "src\main\java\com\vetexpert\sistema_veterinaria"
$BASE_PKG = "com.vetexpert.sistema_veterinaria"

Write-Host "== FASE 1 - Reestructuracion de Arquitectura ==" -ForegroundColor Cyan

function EnsureDir($path) {
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

function CreatePackageInfo($dir, $pkg) {
    EnsureDir $dir
    $file = "$dir\package-info.java"
    if (-not (Test-Path $file)) {
        $c = "/**`n * Modulo: $pkg`n * Preparado para migracion a microservicios - Fase 1.`n */`npackage $pkg;"
        [System.IO.File]::WriteAllText((Resolve-Path $dir -ErrorAction Stop).Path + "\package-info.java", $c, [System.Text.Encoding]::UTF8)
    }
}

function MoveJavaFile($srcFile, $destFile, $oldPkg, $newPkg) {
    if (-not (Test-Path $srcFile)) {
        Write-Host "SKIP: $srcFile" -ForegroundColor Yellow
        return
    }
    $content = [System.IO.File]::ReadAllText($srcFile, [System.Text.Encoding]::UTF8)
    $oldDecl = "package " + $oldPkg + ";"
    $newDecl = "package " + $newPkg + ";"
    $content = $content.Replace($oldDecl, $newDecl)
    EnsureDir (Split-Path $destFile)
    [System.IO.File]::WriteAllText($destFile, $content, [System.Text.Encoding]::UTF8)
    Remove-Item $srcFile -Force
    Write-Host "OK: $(Split-Path $srcFile -Leaf)" -ForegroundColor Green
}

function ReplaceInFile($file, $from, $to) {
    if (-not (Test-Path $file)) { return $false }
    $content = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8)
    if ($content.Contains($from)) {
        $content = $content.Replace($from, $to)
        [System.IO.File]::WriteAllText($file, $content, [System.Text.Encoding]::UTF8)
        return $true
    }
    return $false
}

function ApplyReplacementsInProject($replacements) {
    $allJava = Get-ChildItem -Recurse -Filter "*.java" -Path $BASE | Select-Object -ExpandProperty FullName
    foreach ($f in $allJava) {
        $content = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8)
        $modified = $false
        foreach ($rep in $replacements) {
            if ($content.Contains($rep.From)) {
                $content = $content.Replace($rep.From, $rep.To)
                $modified = $true
            }
        }
        if ($modified) {
            [System.IO.File]::WriteAllText($f, $content, [System.Text.Encoding]::UTF8)
            Write-Host "IMPORTS: $(Split-Path $f -Leaf)" -ForegroundColor Cyan
        }
    }
}

function CleanEmptyDirs($dirs) {
    foreach ($d in $dirs) {
        $path = "$BASE\$d"
        if ((Test-Path $path) -and ((Get-ChildItem $path -Recurse -File).Count -eq 0)) {
            Remove-Item $path -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

# ============================================================
Write-Host "FASE A: Crear subcarpetas placeholder..." -ForegroundColor Yellow

$extraFolders = @(
    @{ dir="agenda";          subs=@("mapper","validator","exception") },
    @{ dir="caja";            subs=@("dto","mapper","validator","exception") },
    @{ dir="historias";       subs=@("mapper","validator","exception") },
    @{ dir="hospitalizacion"; subs=@("dto","mapper","validator","exception") },
    @{ dir="inventario";      subs=@("dto","mapper","validator","exception") },
    @{ dir="mascotas";        subs=@("mapper","validator","exception") },
    @{ dir="propietarios";    subs=@("dto","mapper","validator","exception") }
)

foreach ($m in $extraFolders) {
    foreach ($sub in $m.subs) {
        $dir = "$BASE\$($m.dir)\$sub"
        EnsureDir $dir
        $pkg = $BASE_PKG + "." + $m.dir + "." + $sub
        CreatePackageInfo $dir $pkg
    }
}

# Modulos nuevos completos
$newMods = @(
    @{ name="notificaciones"; subs=@("controller","entity","repository","service","dto","mapper","validator","exception") },
    @{ name="reportes";       subs=@("controller","entity","repository","service","dto","mapper","validator","exception") },
    @{ name="utils";          subs=@() },
    @{ name="shared";         subs=@() }
)
foreach ($m in $newMods) {
    $dir = "$BASE\$($m.name)"
    EnsureDir $dir
    CreatePackageInfo $dir ($BASE_PKG + "." + $m.name)
    foreach ($sub in $m.subs) {
        EnsureDir "$dir\$sub"
        CreatePackageInfo "$dir\$sub" ($BASE_PKG + "." + $m.name + "." + $sub)
    }
}

Write-Host "FASE A COMPLETA" -ForegroundColor Green

# ============================================================
Write-Host "FASE B: Mover SecurityInterceptor y HomeController..." -ForegroundColor Yellow

EnsureDir "$BASE\security"
CreatePackageInfo "$BASE\security" ($BASE_PKG + ".security")
MoveJavaFile "$BASE\config\SecurityInterceptor.java" "$BASE\security\SecurityInterceptor.java" `
    ($BASE_PKG + ".config") ($BASE_PKG + ".security")

ReplaceInFile "$BASE\config\WebMvcConfig.java" `
    ($BASE_PKG + ".config.SecurityInterceptor") `
    ($BASE_PKG + ".security.SecurityInterceptor") | Out-Null
Write-Host "OK: WebMvcConfig import actualizado" -ForegroundColor Green

EnsureDir "$BASE\dashboard\controller"
CreatePackageInfo "$BASE\dashboard" ($BASE_PKG + ".dashboard")
MoveJavaFile "$BASE\config\HomeController.java" "$BASE\dashboard\controller\HomeController.java" `
    ($BASE_PKG + ".config") ($BASE_PKG + ".dashboard.controller")

Write-Host "FASE B COMPLETA" -ForegroundColor Green

# ============================================================
Write-Host "FASE C: Renombrar model a entity en todos los modulos..." -ForegroundColor Yellow

$modelModules = @(
    @{ mod="agenda";          files=@("Cita","EstadoCita","PrioridadCita","TipoCita") },
    @{ mod="caja";            files=@("Venta","DetalleVenta","EstadoPago","TipoComprobante") },
    @{ mod="historias";       files=@("HistoriaClinica","EstadoConsulta") },
    @{ mod="hospitalizacion"; files=@("Hospitalizacion") },
    @{ mod="inventario";      files=@("Producto","Proveedor","Movimiento") },
    @{ mod="mascotas";        files=@("Mascota","Especie","Sexo") },
    @{ mod="propietarios";    files=@("Propietario") }
)

foreach ($m in $modelModules) {
    $oldDir = "$BASE\$($m.mod)\model"
    $newDir = "$BASE\$($m.mod)\entity"
    $oldPkg = $BASE_PKG + "." + $m.mod + ".model"
    $newPkg = $BASE_PKG + "." + $m.mod + ".entity"
    EnsureDir $newDir
    foreach ($f in $m.files) {
        MoveJavaFile "$oldDir\$f.java" "$newDir\$f.java" $oldPkg $newPkg
    }
    if ((Test-Path $oldDir) -and ((Get-ChildItem $oldDir).Count -eq 0)) {
        Remove-Item $oldDir -Force
    }
}

$modelReps = @(
    @{ From=$BASE_PKG+".agenda.model.";          To=$BASE_PKG+".agenda.entity." },
    @{ From=$BASE_PKG+".caja.model.";            To=$BASE_PKG+".caja.entity." },
    @{ From=$BASE_PKG+".historias.model.";       To=$BASE_PKG+".historias.entity." },
    @{ From=$BASE_PKG+".hospitalizacion.model."; To=$BASE_PKG+".hospitalizacion.entity." },
    @{ From=$BASE_PKG+".inventario.model.";      To=$BASE_PKG+".inventario.entity." },
    @{ From=$BASE_PKG+".mascotas.model.";        To=$BASE_PKG+".mascotas.entity." },
    @{ From=$BASE_PKG+".propietarios.model.";    To=$BASE_PKG+".propietarios.entity." }
)
ApplyReplacementsInProject $modelReps

Write-Host "FASE C COMPLETA" -ForegroundColor Green

# ============================================================
Write-Host "FASE D: Renombrar auth a usuarios..." -ForegroundColor Yellow

$authFiles = @(
    @{ s="auth\controller\AuthController.java";         d="usuarios\controller\AuthController.java" },
    @{ s="auth\controller\PortalLoginController.java";  d="usuarios\controller\PortalLoginController.java" },
    @{ s="auth\controller\UsuarioController.java";      d="usuarios\controller\UsuarioController.java" },
    @{ s="auth\model\OtpVerification.java";             d="usuarios\entity\OtpVerification.java" },
    @{ s="auth\model\Usuario.java";                     d="usuarios\entity\Usuario.java" },
    @{ s="auth\repository\OtpVerificationRepository.java"; d="usuarios\repository\OtpVerificationRepository.java" },
    @{ s="auth\repository\UsuarioRepository.java";      d="usuarios\repository\UsuarioRepository.java" },
    @{ s="auth\service\AuthService.java";               d="usuarios\service\AuthService.java" },
    @{ s="auth\service\EmailService.java";              d="usuarios\service\EmailService.java" },
    @{ s="auth\service\EmailVerificationService.java";  d="usuarios\service\EmailVerificationService.java" },
    @{ s="auth\service\WhatsAppOtpService.java";        d="usuarios\service\WhatsAppOtpService.java" },
    @{ s="auth\service\impl\AuthServiceImpl.java";      d="usuarios\service\impl\AuthServiceImpl.java" }
)

$authPkgReps = @(
    @{ From=$BASE_PKG+".auth.controller."; To=$BASE_PKG+".usuarios.controller." },
    @{ From=$BASE_PKG+".auth.model.";      To=$BASE_PKG+".usuarios.entity." },
    @{ From=$BASE_PKG+".auth.repository."; To=$BASE_PKG+".usuarios.repository." },
    @{ From=$BASE_PKG+".auth.service.impl."; To=$BASE_PKG+".usuarios.service.impl." },
    @{ From=$BASE_PKG+".auth.service.";    To=$BASE_PKG+".usuarios.service." }
)

foreach ($f in $authFiles) {
    $src = "$BASE\$($f.s)"
    $dst = "$BASE\$($f.d)"
    if (-not (Test-Path $src)) { Write-Host "SKIP: $src" -ForegroundColor Yellow; continue }
    $content = [System.IO.File]::ReadAllText($src, [System.Text.Encoding]::UTF8)
    # Determinar paquete viejo
    $parts = $f.s -split '\\'
    $subparts = $parts[1..($parts.Length-2)] -join '.'
    $oldPkg = $BASE_PKG + ".auth." + $subparts
    $dparts = $f.d -split '\\'
    $dsubparts = $dparts[1..($dparts.Length-2)] -join '.'
    $newPkg = $BASE_PKG + ".usuarios." + $dsubparts
    $content = $content.Replace("package " + $oldPkg + ";", "package " + $newPkg + ";")
    foreach ($rep in $authPkgReps) {
        $content = $content.Replace($rep.From, $rep.To)
    }
    EnsureDir (Split-Path $dst)
    [System.IO.File]::WriteAllText($dst, $content, [System.Text.Encoding]::UTF8)
    Remove-Item $src -Force
    Write-Host "OK: $(Split-Path $src -Leaf)" -ForegroundColor Green
}

foreach ($sub in @("dto","mapper","validator","exception")) {
    CreatePackageInfo "$BASE\usuarios\$sub" ($BASE_PKG + ".usuarios." + $sub)
}

ApplyReplacementsInProject $authPkgReps

CleanEmptyDirs @("auth\controller","auth\model","auth\repository","auth\service\impl","auth\service","auth")

Write-Host "FASE D COMPLETA" -ForegroundColor Green

# ============================================================
Write-Host "FASE E: Mover servicios a shared.servicios..." -ForegroundColor Yellow

$serviciosFiles = @(
    @{ s="servicios\model\Servicio.java";                    d="shared\servicios\entity\Servicio.java" },
    @{ s="servicios\repository\ServicioRepository.java";     d="shared\servicios\repository\ServicioRepository.java" },
    @{ s="servicios\service\ServicioService.java";           d="shared\servicios\service\ServicioService.java" },
    @{ s="servicios\service\impl\ServicioServiceImpl.java";  d="shared\servicios\service\impl\ServicioServiceImpl.java" }
)

$serviciosPkgReps = @(
    @{ From=$BASE_PKG+".servicios.model.";        To=$BASE_PKG+".shared.servicios.entity." },
    @{ From=$BASE_PKG+".servicios.repository.";   To=$BASE_PKG+".shared.servicios.repository." },
    @{ From=$BASE_PKG+".servicios.service.impl."; To=$BASE_PKG+".shared.servicios.service.impl." },
    @{ From=$BASE_PKG+".servicios.service.";      To=$BASE_PKG+".shared.servicios.service." }
)

foreach ($f in $serviciosFiles) {
    $src = "$BASE\$($f.s)"
    $dst = "$BASE\$($f.d)"
    if (-not (Test-Path $src)) { Write-Host "SKIP: $src" -ForegroundColor Yellow; continue }
    $content = [System.IO.File]::ReadAllText($src, [System.Text.Encoding]::UTF8)
    $parts = $f.s -split '\\'
    $subparts = $parts[1..($parts.Length-2)] -join '.'
    $oldPkg = $BASE_PKG + ".servicios." + $subparts
    $dparts = $f.d -split '\\'
    $dsubparts = $dparts[1..($dparts.Length-2)] -join '.'
    $newPkg = $BASE_PKG + ".shared.servicios." + $dsubparts
    $content = $content.Replace("package " + $oldPkg + ";", "package " + $newPkg + ";")
    foreach ($rep in $serviciosPkgReps) {
        $content = $content.Replace($rep.From, $rep.To)
    }
    EnsureDir (Split-Path $dst)
    [System.IO.File]::WriteAllText($dst, $content, [System.Text.Encoding]::UTF8)
    Remove-Item $src -Force
    Write-Host "OK: $(Split-Path $src -Leaf)" -ForegroundColor Green
}

CreatePackageInfo "$BASE\shared" ($BASE_PKG + ".shared")
CreatePackageInfo "$BASE\shared\servicios" ($BASE_PKG + ".shared.servicios")

ApplyReplacementsInProject $serviciosPkgReps

CleanEmptyDirs @("servicios\service\impl","servicios\service","servicios\repository","servicios\model","servicios")

Write-Host "FASE E COMPLETA" -ForegroundColor Green

# ============================================================
Write-Host "FASE F: Fusionar publico y promociones en portal..." -ForegroundColor Yellow

$portalFiles = @(
    @{ s="publico\controller\PortalClienteController.java";      d="portal\controller\PortalClienteController.java" },
    @{ s="publico\controller\PublicAuthController.java";         d="portal\controller\PublicAuthController.java" },
    @{ s="publico\controller\PublicController.java";             d="portal\controller\PublicController.java" },
    @{ s="publico\model\ContactoMensaje.java";                   d="portal\entity\ContactoMensaje.java" },
    @{ s="publico\repository\ContactoMensajeRepository.java";    d="portal\repository\ContactoMensajeRepository.java" },
    @{ s="publico\service\ContactoMensajeService.java";          d="portal\service\ContactoMensajeService.java" },
    @{ s="publico\service\impl\ContactoMensajeServiceImpl.java"; d="portal\service\impl\ContactoMensajeServiceImpl.java" },
    @{ s="promociones\controller\PromocionController.java";      d="portal\controller\PromocionController.java" },
    @{ s="promociones\model\Promocion.java";                     d="portal\entity\Promocion.java" },
    @{ s="promociones\repository\PromocionRepository.java";      d="portal\repository\PromocionRepository.java" },
    @{ s="promociones\service\PromocionService.java";            d="portal\service\PromocionService.java" },
    @{ s="promociones\service\impl\PromocionServiceImpl.java";   d="portal\service\impl\PromocionServiceImpl.java" }
)

$portalPkgReps = @(
    @{ From=$BASE_PKG+".publico.model.";             To=$BASE_PKG+".portal.entity." },
    @{ From=$BASE_PKG+".publico.repository.";        To=$BASE_PKG+".portal.repository." },
    @{ From=$BASE_PKG+".publico.service.impl.";      To=$BASE_PKG+".portal.service.impl." },
    @{ From=$BASE_PKG+".publico.service.";           To=$BASE_PKG+".portal.service." },
    @{ From=$BASE_PKG+".publico.controller.";        To=$BASE_PKG+".portal.controller." },
    @{ From=$BASE_PKG+".promociones.model.";         To=$BASE_PKG+".portal.entity." },
    @{ From=$BASE_PKG+".promociones.repository.";    To=$BASE_PKG+".portal.repository." },
    @{ From=$BASE_PKG+".promociones.service.impl.";  To=$BASE_PKG+".portal.service.impl." },
    @{ From=$BASE_PKG+".promociones.service.";       To=$BASE_PKG+".portal.service." },
    @{ From=$BASE_PKG+".promociones.controller.";    To=$BASE_PKG+".portal.controller." }
)

foreach ($f in $portalFiles) {
    $src = "$BASE\$($f.s)"
    $dst = "$BASE\$($f.d)"
    if (-not (Test-Path $src)) { Write-Host "SKIP: $src" -ForegroundColor Yellow; continue }
    $content = [System.IO.File]::ReadAllText($src, [System.Text.Encoding]::UTF8)
    if ($content -match 'package (com\.vetexpert\.sistema_veterinaria\.[^;]+);') {
        $oldPkg = $Matches[1]
        $dparts = $f.d -split '\\'
        $dsubparts = $dparts[1..($dparts.Length-2)] -join '.'
        $newPkg = $BASE_PKG + ".portal." + $dsubparts
        $content = $content.Replace("package " + $oldPkg + ";", "package " + $newPkg + ";")
    }
    foreach ($rep in $portalPkgReps) {
        $content = $content.Replace($rep.From, $rep.To)
    }
    EnsureDir (Split-Path $dst)
    [System.IO.File]::WriteAllText($dst, $content, [System.Text.Encoding]::UTF8)
    Remove-Item $src -Force
    Write-Host "OK: $(Split-Path $src -Leaf)" -ForegroundColor Green
}

CreatePackageInfo "$BASE\portal" ($BASE_PKG + ".portal")
foreach ($sub in @("dto","mapper","validator","exception")) {
    CreatePackageInfo "$BASE\portal\$sub" ($BASE_PKG + ".portal." + $sub)
}

ApplyReplacementsInProject $portalPkgReps

CleanEmptyDirs @(
    "publico\service\impl","publico\service","publico\repository","publico\model","publico\controller","publico",
    "promociones\service\impl","promociones\service","promociones\repository","promociones\model","promociones\controller","promociones"
)

Write-Host "FASE F COMPLETA" -ForegroundColor Green

# ============================================================
Write-Host "FASE G: Separar vacunacion en vacunas y desparasitaciones..." -ForegroundColor Yellow

$vacFiles = @(
    @{ s="vacunacion\controller\VacunaController.java";            d="vacunas\controller\VacunaController.java" },
    @{ s="vacunacion\controller\AlertasController.java";           d="vacunas\controller\AlertasController.java" },
    @{ s="vacunacion\config\VacunacionConfig.java";                d="vacunas\config\VacunacionConfig.java" },
    @{ s="vacunacion\model\Vacuna.java";                           d="vacunas\entity\Vacuna.java" },
    @{ s="vacunacion\model\VacunaAplicada.java";                   d="vacunas\entity\VacunaAplicada.java" },
    @{ s="vacunacion\model\EstadoVacuna.java";                     d="vacunas\entity\EstadoVacuna.java" },
    @{ s="vacunacion\repository\VacunaRepository.java";            d="vacunas\repository\VacunaRepository.java" },
    @{ s="vacunacion\repository\VacunaAplicadaRepository.java";    d="vacunas\repository\VacunaAplicadaRepository.java" },
    @{ s="vacunacion\service\VacunaService.java";                  d="vacunas\service\VacunaService.java" },
    @{ s="vacunacion\service\VacunaAplicadaService.java";          d="vacunas\service\VacunaAplicadaService.java" },
    @{ s="vacunacion\service\AlertaService.java";                  d="vacunas\service\AlertaService.java" },
    @{ s="vacunacion\service\impl\VacunaServiceImpl.java";         d="vacunas\service\impl\VacunaServiceImpl.java" },
    @{ s="vacunacion\service\impl\VacunaAplicadaServiceImpl.java"; d="vacunas\service\impl\VacunaAplicadaServiceImpl.java" },
    @{ s="vacunacion\service\impl\AlertaServiceImpl.java";         d="vacunas\service\impl\AlertaServiceImpl.java" },
    @{ s="vacunacion\dto\VacunaDTO.java";                          d="vacunas\dto\VacunaDTO.java" },
    @{ s="vacunacion\controller\DesparasitacionController.java";   d="desparasitaciones\controller\DesparasitacionController.java" },
    @{ s="vacunacion\model\Desparasitacion.java";                  d="desparasitaciones\entity\Desparasitacion.java" },
    @{ s="vacunacion\model\TipoDesparasitacion.java";              d="desparasitaciones\entity\TipoDesparasitacion.java" },
    @{ s="vacunacion\repository\DesparasitacionRepository.java";   d="desparasitaciones\repository\DesparasitacionRepository.java" },
    @{ s="vacunacion\service\DesparasitacionService.java";         d="desparasitaciones\service\DesparasitacionService.java" },
    @{ s="vacunacion\service\impl\DesparasitacionServiceImpl.java"; d="desparasitaciones\service\impl\DesparasitacionServiceImpl.java" },
    @{ s="vacunacion\dto\DesparasitacionDTO.java";                 d="desparasitaciones\dto\DesparasitacionDTO.java" }
)

# Tabla de donde va cada clase de desparasitacion
$despClasses = @("Desparasitacion","TipoDesparasitacion","DesparasitacionRepository","DesparasitacionService","DesparasitacionServiceImpl","DesparasitacionDTO","DesparasitacionController")

foreach ($f in $vacFiles) {
    $src = "$BASE\$($f.s)"
    $dst = "$BASE\$($f.d)"
    if (-not (Test-Path $src)) { Write-Host "SKIP: $src" -ForegroundColor Yellow; continue }
    $content = [System.IO.File]::ReadAllText($src, [System.Text.Encoding]::UTF8)
    $isDesp = $f.d.StartsWith("desparasitaciones")
    
    if ($content -match 'package (com\.vetexpert\.sistema_veterinaria\.[^;]+);') {
        $oldPkg = $Matches[1]
        $dparts = $f.d -split '\\'
        $dsubparts = $dparts[1..($dparts.Length-2)] -join '.'
        $targetMod = if ($isDesp) { "desparasitaciones" } else { "vacunas" }
        $newPkg = $BASE_PKG + "." + $targetMod + "." + $dsubparts
        $content = $content.Replace("package " + $oldPkg + ";", "package " + $newPkg + ";")
    }
    
    # Reemplazar imports vacunacion.* a vacunas.*
    $content = $content.Replace($BASE_PKG + ".vacunacion.model.", $BASE_PKG + ".vacunas.entity.")
    $content = $content.Replace($BASE_PKG + ".vacunacion.config.", $BASE_PKG + ".vacunas.config.")
    $content = $content.Replace($BASE_PKG + ".vacunacion.repository.", $BASE_PKG + ".vacunas.repository.")
    $content = $content.Replace($BASE_PKG + ".vacunacion.service.impl.", $BASE_PKG + ".vacunas.service.impl.")
    $content = $content.Replace($BASE_PKG + ".vacunacion.service.", $BASE_PKG + ".vacunas.service.")
    $content = $content.Replace($BASE_PKG + ".vacunacion.controller.", $BASE_PKG + ".vacunas.controller.")
    $content = $content.Replace($BASE_PKG + ".vacunacion.dto.", $BASE_PKG + ".vacunas.dto.")
    
    # Corregir las clases de desparasitacion que quedaron apuntando a vacunas
    $content = $content.Replace($BASE_PKG + ".vacunas.entity.Desparasitacion", $BASE_PKG + ".desparasitaciones.entity.Desparasitacion")
    $content = $content.Replace($BASE_PKG + ".vacunas.entity.TipoDesparasitacion", $BASE_PKG + ".desparasitaciones.entity.TipoDesparasitacion")
    $content = $content.Replace($BASE_PKG + ".vacunas.repository.DesparasitacionRepository", $BASE_PKG + ".desparasitaciones.repository.DesparasitacionRepository")
    $content = $content.Replace($BASE_PKG + ".vacunas.service.DesparasitacionService", $BASE_PKG + ".desparasitaciones.service.DesparasitacionService")
    $content = $content.Replace($BASE_PKG + ".vacunas.service.impl.DesparasitacionServiceImpl", $BASE_PKG + ".desparasitaciones.service.impl.DesparasitacionServiceImpl")
    $content = $content.Replace($BASE_PKG + ".vacunas.dto.DesparasitacionDTO", $BASE_PKG + ".desparasitaciones.dto.DesparasitacionDTO")
    $content = $content.Replace($BASE_PKG + ".vacunas.controller.DesparasitacionController", $BASE_PKG + ".desparasitaciones.controller.DesparasitacionController")
    
    EnsureDir (Split-Path $dst)
    [System.IO.File]::WriteAllText($dst, $content, [System.Text.Encoding]::UTF8)
    Remove-Item $src -Force
    Write-Host "OK: $(Split-Path $src -Leaf)" -ForegroundColor Green
}

# Placeholders
CreatePackageInfo "$BASE\vacunas" ($BASE_PKG + ".vacunas")
CreatePackageInfo "$BASE\desparasitaciones" ($BASE_PKG + ".desparasitaciones")
foreach ($sub in @("mapper","validator","exception")) {
    CreatePackageInfo "$BASE\vacunas\$sub" ($BASE_PKG + ".vacunas." + $sub)
    CreatePackageInfo "$BASE\desparasitaciones\$sub" ($BASE_PKG + ".desparasitaciones." + $sub)
}

# Actualizar imports en todo el proyecto
$vacReps = @(
    @{ From=$BASE_PKG+".vacunacion.model.";        To=$BASE_PKG+".vacunas.entity." },
    @{ From=$BASE_PKG+".vacunacion.config.";       To=$BASE_PKG+".vacunas.config." },
    @{ From=$BASE_PKG+".vacunacion.repository.";   To=$BASE_PKG+".vacunas.repository." },
    @{ From=$BASE_PKG+".vacunacion.service.impl."; To=$BASE_PKG+".vacunas.service.impl." },
    @{ From=$BASE_PKG+".vacunacion.service.";      To=$BASE_PKG+".vacunas.service." },
    @{ From=$BASE_PKG+".vacunacion.controller.";   To=$BASE_PKG+".vacunas.controller." },
    @{ From=$BASE_PKG+".vacunacion.dto.";          To=$BASE_PKG+".vacunas.dto." }
)
ApplyReplacementsInProject $vacReps

# Segundo pase: corregir desparasitacion→desparasitaciones en todo el proyecto
$despReps = @(
    @{ From=$BASE_PKG+".vacunas.entity.Desparasitacion";      To=$BASE_PKG+".desparasitaciones.entity.Desparasitacion" },
    @{ From=$BASE_PKG+".vacunas.entity.TipoDesparasitacion";  To=$BASE_PKG+".desparasitaciones.entity.TipoDesparasitacion" },
    @{ From=$BASE_PKG+".vacunas.repository.DesparasitacionRepository"; To=$BASE_PKG+".desparasitaciones.repository.DesparasitacionRepository" },
    @{ From=$BASE_PKG+".vacunas.service.DesparasitacionService"; To=$BASE_PKG+".desparasitaciones.service.DesparasitacionService" },
    @{ From=$BASE_PKG+".vacunas.service.impl.DesparasitacionServiceImpl"; To=$BASE_PKG+".desparasitaciones.service.impl.DesparasitacionServiceImpl" },
    @{ From=$BASE_PKG+".vacunas.dto.DesparasitacionDTO";       To=$BASE_PKG+".desparasitaciones.dto.DesparasitacionDTO" },
    @{ From=$BASE_PKG+".vacunas.controller.DesparasitacionController"; To=$BASE_PKG+".desparasitaciones.controller.DesparasitacionController" }
)
ApplyReplacementsInProject $despReps

CleanEmptyDirs @(
    "vacunacion\service\impl","vacunacion\service","vacunacion\repository","vacunacion\model",
    "vacunacion\dto","vacunacion\controller","vacunacion\config","vacunacion"
)

Write-Host "FASE G COMPLETA" -ForegroundColor Green

# ============================================================
Write-Host "" 
Write-Host "== REESTRUCTURACION COMPLETADA ==" -ForegroundColor Green
Write-Host "Ejecuta ahora: mvn clean compile" -ForegroundColor Yellow
Write-Host ""
Write-Host "Estructura final de modulos:" -ForegroundColor White
Get-ChildItem "$BASE" -Directory | Sort-Object Name | ForEach-Object {
    $mod = $_.Name
    $subs = (Get-ChildItem $_.FullName -Directory | Sort-Object Name | ForEach-Object { $_.Name }) -join ", "
    Write-Host "  /$mod  [$subs]" -ForegroundColor Gray
}
