return {
  {
    "mfussenegger/nvim-lint",
    event = { "BufReadPre", "BufNewFile" },
    config = function()
      local lint = require("lint")

      lint.linters_by_ft = {
        yaml = { "yamllint" },
      }
      -- Creamos un grupo de autocomandos para que no se dupliquen
      local lint_augroup = vim.api.nvim_create_augroup("lint", { clear = true })

      -- Se ejecuta al entrar al buffer, al insertar texto o al guardar
      vim.api.nvim_create_autocmd({ "BufEnter", "BufWritePost", "InsertLeave" }, {
        group = lint_augroup,
        callback = function()
          lint.try_lint()
        end,
      })
    end,
  },
  -- Opcional: Instalación automática con Mason
  {
    "WhoIsSethDaniel/mason-tool-installer.nvim",
    dependencies = {
      "williamboman/mason.nvim",
    },
    config = function()
      require("mason-tool-installer").setup({
        -- Lista de herramientas a instalar automáticamente
        ensure_installed = {
          "yamllint",       -- El linter para yaml
        },

        -- Si es true, instala las herramientas al arrancar Neovim
        auto_install = true,

        -- Ejecuta la instalación al inicio (puedes desactivarlo si prefieres hacerlo manual)
        run_on_start = true,
        
        -- Tiempo de espera antes de empezar la instalación (en ms)
        start_delay = 3000, -- 3 segundos para no ralentizar el inicio inmediato
      })
    end,
  },
}
