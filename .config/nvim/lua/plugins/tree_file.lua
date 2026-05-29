-- File explorer
return {
  {
    "nvim-tree/nvim-tree.lua",
    version = "*",
    lazy = false, -- Se recomienda false si quieres que nvim-tree detecte cambios de archivos al abrir
    dependencies = { "nvim-tree/nvim-web-devicons" },
    keys = {
      { "<leader>e", "<cmd>NvimTreeToggle<cr>", desc = "Toggle Explorer" },
      { "<leader>o", "<cmd>NvimTreeFocus<cr>", desc = "Focus Explorer" },
    },
    config = function()
      -- Recomendado: deshabilitar netrw (el explorador nativo de Vim)
      vim.g.loaded_netrw = 1
      vim.g.loaded_netrwPlugin = 1

      require("nvim-tree").setup({
        view = { 
          width = 34,
          relativenumber = true, -- Útil para saltar entre archivos con números
        },
        renderer = { 
          group_empty = true,
          highlight_git = true, -- Resalta archivos modificados por Git
        },
        filters = { dotfiles = false },
        git = { enable = true },
      })
    end,
  },
}

