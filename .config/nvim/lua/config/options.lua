local opt = vim.opt

opt.number = true          -- Muestra números de línea
opt.tabstop = 2            -- Tamaño de tabulación
opt.shiftwidth = 2         -- Tamaño de indentación
opt.expandtab = true       -- Transforma tabs en espacios
opt.smartindent = true     -- Indentación inteligente
-- opt.mouse = "a"            -- Habilita el mouse (por si acaso)
opt.clipboard = "unnamedplus" -- Sincroniza con el portapapeles del sistema
vim.o.splitright = true
vim.o.splitbelow = true
opt.termguicolors = true   -- Colores reales en la terminal
opt.modeline = false

-------------------------------------
---FOLD
-------------------------------------
-- '1' muestra una columna delgada, '0' la oculta.
vim.opt.foldcolumn = "1" 

-- Opcional: Personaliza los caracteres que usa Neovim para los pliegues
vim.opt.fillchars = {
  foldopen = "", -- Icono para pliegue abierto
  foldclose = "", -- Icono para pliegue cerrado
  fold = " ",      -- Carácter de relleno
  foldsep = " ",   -- Separador
}
