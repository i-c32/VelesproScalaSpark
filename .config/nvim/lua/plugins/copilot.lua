-- Copilot (inline)
return {
  {
    "github/copilot.vim",
    event = "InsertEnter",
    config = function()
      -- Optional: disable default <Tab> mapping if you prefer cmp handles Tab
      vim.g.copilot_no_tab_map = true
      vim.api.nvim_set_keymap("i", "<C-J>", 'copilot#Accept("<CR>")', { expr = true, silent = true })
      vim.g.copilot_filetypes = { ["*"] = true }
    end,
  },

  -- CodeCompanion
  {
    "olimorris/codecompanion.nvim",
    branch = "main",
    dependencies = {
      "nvim-lua/plenary.nvim",
      "nvim-treesitter/nvim-treesitter",
      "saghen/blink.cmp", -- Usamos blink en lugar de nvim-cmp
      { "stevearc/dressing.nvim", opts = {} },
    },
    opts = {
      -- 1. DEFINICIÓN DE ADAPTADORES
      adapters = {
        http = {
          -- GEMINI (External via API Key)
          gemini = function()
            return require("codecompanion.adapters").extend("gemini", {
              env = { 
                api_key = os.getenv("GEMINI_API_KEY"),
              },
            })
          end,
          -- Openai (External via API Key)
          openai = function()
            return require("codecompanion.adapters").extend("openai", {
              env = { 
                api_key = os.getenv("OPENAI_API_KEY"),
              },
            })
          end,
          -- COPILOT (Native)
          copilot = function()
            return require("codecompanion.adapters").extend("copilot", {})
          end,
        },
      },

      -- 2. ESTRATEGIAS Y KEYMAPS DEL CHAT
      interactions = {
        chat = { 
          adapter = {
            name = "gemini",
            model = "gemini-2.5-flash-lite",
            slash_commands = {
              ["buffer"] = {
                opts = {
                  contains_code = true,
                  display_name = "Buffer",
                  filter_list = true,
                },
              },
            },
          },
        },
        inline = { adapter = "gemini" },
        agent = { adapter = "copilot" },
      },
      display = {
        chat = {
          show_adapter_name = true, -- Muestra el nombre del adaptador (Gemini)
          show_token_count = true,   -- Útil para el Free Tier
          window = {
            layout = "vertical", -- O "horizontal" si prefieres el estilo anterior
            width = 0.35,
            title = "CodeCompanion Chat (%s)",
          },
        },
      },
    },
    keys = {
      { "<leader>cc", "<cmd>CodeCompanionChat Toggle<cr>", desc = "CodeCompanion Chat toggle" },
      { "<leader>ca", "<cmd>CodeCompanionActions<cr>", desc = "CodeCompanion Actions" },
      { "ga", "<cmd>CodeCompanionChat Add<cr>", mode = "v", desc = "Añadir selección al chat" },
      { "<leader>ci", "<cmd>CodeCompanion<cr>", mode = "n", desc = "CodeCompanion Inline" },
    },
  },
}
