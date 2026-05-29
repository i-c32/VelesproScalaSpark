return {
  {
    'nvim-telescope/telescope.nvim',
    branch = 'master', -- Te recomiendo usar una versión estable
    dependencies = { 
      'nvim-lua/plenary.nvim',
      { 'nvim-telescope/telescope-fzf-native.nvim', build = 'make' } -- Para búsquedas ultra rápidas
    },
    config = function()
      local telescope = require("telescope")
      
      telescope.setup({
        defaults = {
          mappings = {
            i = {
              ["<C-j>"] = "move_selection_next",
              ["<C-k>"] = "move_selection_previous",
            },
          },
          -- Estilo visual más limpio
          borderchars = { "─", "│", "─", "│", "┌", "┐", "┘", "└" },
        },
      })

      -- Cargar la extensión de búsqueda rápida si se compiló bien
      pcall(telescope.load_extension, 'fzf')

      -- 2. ATAJOS DE TECLADO (Keymaps)
      local builtin = require('telescope.builtin')
      vim.keymap.set('n', '<leader>ff', builtin.find_files, { desc = 'Buscar archivos' })
      vim.keymap.set('n', '<leader>fg', builtin.live_grep, { desc = 'Buscar texto (grep)' })
      vim.keymap.set('n', '<leader>fb', builtin.buffers, { desc = 'Listar buffers abiertos' })
      vim.keymap.set('n', '<leader>fh', builtin.help_tags, { desc = 'Buscar en la ayuda' })
      -- Muy útil para LSP:
      vim.keymap.set('n', '<leader>fs', builtin.lsp_document_symbols, { desc = 'Símbolos del documento' })
    end
  }
}
