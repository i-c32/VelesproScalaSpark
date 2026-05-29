NVIM_COMMANDS(1) "Chuleta con los comandos de nvim" "Marzo 2026"

# NAME

**Chuleta nvim** - comandos para ejecutar scripts de nvim

# DESCRIPTION

Comandos para ejecutar neovim y sus plugins.

# PLUGINS
## nvim-tree
<leader>+e
	Abrir el arbol de ficheros o cerrarlo.

r
	Renombrar el fichero.

## Telescope

## LSP
gd
	Modo normal sobre una función te lleva a su definicion.

K 
	Modo normal muestra la información de lo que tienes bajo el cursor.

<>+e 
	Cuando estas sobre una línea con error o warning los muestra, si se vuelve a
	dar se entra en este menú para poder copiar los errores con neovim.

Crtl + y
	Aceptar la sugerencia.

Crtl + n
	Siguiente sugerencia.

:LspLog
	Para ver los logs cuando el lsp da errores.

## CodeCompanion

<leader> + c + c
	Ejecutar el chat de copilot.

## Copilot

:Copilot setup
	Para configurar github. Copiar el código y pegarlo en el navegador, si estas logeado en github es directo esto.

## Terminal

<leader> + t
	Para lanzar la terminal, si le das a Esc te pasas al modo normal que te permite moverte a las otras ventanas con Crtl+w y las flechas de dirección.

## Check Plugins

:checkhealth nvim-treesitter
	Comprueba los errores del plugin.

# NEOVIM
:'<,'>w !bash
	Para ejecutar comandos desde nvim, primero se selecciona las líneas con el modo visual y se ejecuta.

:lua print(vim.g.mapleader)
	Para saber cual es la tecla leader de vim, si es nil es \, que es la que esta por defecto

zc
	Para cerrar las secciones folder
