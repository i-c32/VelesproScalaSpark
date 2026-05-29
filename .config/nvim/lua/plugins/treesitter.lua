-- Tree-sitter
return {
  {
    "nvim-treesitter/nvim-treesitter",
    branch = "main",
    build = ":TSUpdate",
    version = "0.10.0", -- Ajustar dependiendo de la version del tree-sitter-cli instalada en el ordenador
    event = { "BufReadPost", "BufNewFile" },
    cmd = { "TSUpdateSync", "TSUpdate", "TSInstall" },
    config = function()
      -- Usamos una configuración protegida para evitar el error de "module not found"
      local status_ok, configs = pcall(require, "nvim-treesitter.configs")
      if not status_ok then return end

      configs.setup({
        -- Instalamos los lenguajes que pediste
        ensure_installed = { 
          "python", 
          "java", 
          "lua", 
          "vim", 
          "markdown", 
          "markdown_inline" 
        },
        highlight = { enable = true },
        indent = { enable = true },
      })
      -- Forzar el método de plegado inmediatamente
      vim.opt.foldmethod = "expr"
      vim.opt.foldexpr = "nvim_treesitter#foldexpr()"
      vim.opt.foldlevel = 8
    end,
  }
}
