return {
  {
    "neovim/nvim-lspconfig",
    dependencies = {
      "williamboman/mason.nvim",
      "williamboman/mason-lspconfig.nvim",
    },
    config = function()
      -- 1. Inicializar Mason
      require("mason").setup()
      require("mason-lspconfig").setup({
        ensure_installed = { "taplo", "jsonls", "yamlls", "basedpyright", "ruff" },
      })

      -- 2. Configuración de Diagnósticos (Para ver quién es quién)
      vim.diagnostic.config({
        virtual_text = {
          source = "always", -- Muestra [ruff] o [pyright] en la línea
          prefix = '●',
        },
        float = {
          source = "always", -- Muestra el origen en la ventana flotante
          border = "rounded",
        },
      })

      -- 3. Configurar basedpyright (Análisis de tipos)
      vim.lsp.config('basedpyright', {})
      
      -- 4. Configurar Ruff
      vim.lsp.config('ruff', {})

      -- 5. Configuramos Taplo (Nuevo Estándar)
      vim.lsp.config('taplo', {
        settings = {
          evenBetterToml = {
            schema = {
              enabled = false, -- Esto desactiva la validación contra esquemas online
            },
          },
        },
      })

      -- 6. Configuración de YAML (Nuevo Estándar 0.11+)
      vim.lsp.config('yamlls', {
        settings = {
          yaml = {
            schemas = {
              -- Esto habilita esquemas automáticos para archivos comunes
              ["https://json.schemastore.org/github-workflow.json"] = "/.github/workflows/*",
              ["../path/to/schema.json"] = "config.yaml",
            },
            validate = true,
            hover = true,
            completion = true,
          },
        },
      })

      -- 7. Configuramos JSON (Nuevo Estándar 0.11+)
      vim.lsp.config('jsonls', {
        settings = {
          json = {
            schemas = require('schemastore').json.schemas(),
            validate = { enable = true },
          },
        },
      })

      -- 8. Habilitar servidores
      vim.lsp.enable("basedpyright")
      vim.lsp.enable("ruff")
      vim.lsp.enable("yamlls")
      vim.lsp.enable("jsonls")

      -- 9. Atajos de teclado
      vim.api.nvim_create_autocmd('LspAttach', {
        callback = function(args)
          local opts = { buffer = args.buf }
          vim.keymap.set('n', 'gd', vim.lsp.buf.definition, opts)
          vim.keymap.set('n', 'K', vim.lsp.buf.hover, opts)
          vim.keymap.set('n', '<leader>e', vim.diagnostic.open_float, opts)
          -- Atajo para que Ruff arregle cosas automáticamente
          vim.keymap.set('n', '<leader>ca', vim.lsp.buf.code_action, opts)
        end,
      })
    end,
  }
}
