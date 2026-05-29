-- Apply the built-in industry theme
vim.cmd.colorscheme("industry")

-- Para iniciar en la misma linea que se cerro
vim.api.nvim_create_autocmd("BufReadPost", {
  callback = function()
    local mark = vim.api.nvim_buf_get_mark(0, '"')
    local lcount = vim.api.nvim_buf_line_count(0)
    if mark[1] > 0 and mark[1] <= lcount then
      pcall(vim.api.nvim_win_set_cursor, 0, mark)
    end
  end,
})

vim.g.mapleader = " "
vim.g.maplocalleader = ","

-- Para ejecutar el .nvim.lua local del proyecto
vim.o.exrc = true
vim.o.secure = true -- evita ejecutar comandos shell arbitrarios sin confirmar

require("config.options")

local lazypath = vim.fn.stdpath("data") .. "/lazy/lazy.nvim"
if not vim.loop.fs_stat(lazypath) then
  vim.fn.system({
    "git", "clone", "--filter=blob:none",
    "https://github.com/folke/lazy.nvim.git", "--branch=stable", lazypath,
  })
end
vim.opt.rtp:prepend(lazypath)

require("lazy").setup({
  spec = {
    -- Esto le dice a Lazy: "Importa todos los archivos dentro de lua/plugins/"
    { import = "plugins" },
  },
  -- Otras opciones (opcional)
  install = { colorscheme = { "habamax" } },
  checker = { enabled = true },
  lockfile = vim.fn.stdpath("data") .. "/lazy-lock.json", -- ~/.local/share/nvim/lazy-lock.json
})

-- Cabiar las ventanas flotantes
-- Cambiar el fondo a algo más oscuro y el texto más suave
vim.api.nvim_set_hl(0, "NormalFloat", { bg = "#1e1e2e", fg = "#cdd6f4" })
-- Cambiar el borde a un color que no distraiga
vim.api.nvim_set_hl(0, "FloatBorder", { fg = "#585b70", bg = "#1e1e2e" })

-- Pegado desde contenedor
-- En tu init.lua
vim.g.clipboard = {
  name = 'OSC 52',
  copy = {
    ['+'] = require('vim.ui.clipboard.osc52').copy('+'),
    ['*'] = require('vim.ui.clipboard.osc52').copy('*'),
  },
  paste = {
    ['+'] = require('vim.ui.clipboard.osc52').paste('+'),
    ['*'] = require('vim.ui.clipboard.osc52').paste('*'),
  },
}

-- 1. El "Vigilante": Fuerza a Neovim a usar Treesitter al abrir el archivo
vim.api.nvim_create_autocmd({ "FileType" }, {
  pattern = { "python", "java" },
  callback = function()
    -- Usamos vim.schedule para asegurar que esto ocurra DESPUÉS 
    -- de que Neovim termine de cargar el archivo y sus scripts internos
    vim.schedule(function()
      vim.opt_local.foldmethod = "expr"
      vim.opt_local.foldexpr = "nvim_treesitter#foldexpr()"
      
      -- foldlevel = 99 significa que el archivo se abre desplegado.
      -- Si prefieres que se abra todo plegado, cámbialo a 0.
      vim.opt_local.foldlevel = 2
    end)
  end,
})
