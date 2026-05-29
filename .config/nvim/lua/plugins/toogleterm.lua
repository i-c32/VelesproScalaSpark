-- Terminal (bottom split)
return {
  {
    "akinsho/toggleterm.nvim",
    version = "*",
    keys = {
      { "<leader>t", "<cmd>ToggleTerm direction=horizontal<cr>", desc = "Terminal toggle (bottom)" },
    },
    config = function()
      require("toggleterm").setup({
        direction = "horizontal",
        size = 15,
        open_mapping = nil, -- we use <leader>t
        shade_terminals = true,
        start_in_insert = true,
        persist_size = true,
        close_on_exit = true,
      })

      -- Esta función aplica los mapeos solo a los buffers de terminal
      function _G.set_terminal_keymaps()
        local opts = {buffer = 0}
        -- Permite usar <leader>t para CERRAR desde la terminal
        vim.keymap.set('t', '<leader>t', [[<cmd>ToggleTerm<cr>]], opts)
        
        -- Extras muy útiles para navegar:
        vim.keymap.set('t', '<esc>', [[<C-\><C-n>]], opts) -- Esc para modo normal
        vim.keymap.set('t', '<C-h>', [[<Cmd>wincmd h<CR>]], opts) -- Saltar a la izquierda
        vim.keymap.set('t', '<C-j>', [[<Cmd>wincmd j<CR>]], opts) -- Saltar abajo
        vim.keymap.set('t', '<C-k>', [[<Cmd>wincmd k<CR>]], opts) -- Saltar arriba
        vim.keymap.set('t', '<C-l>', [[<Cmd>wincmd l<CR>]], opts) -- Saltar a la derecha
      end

      -- Ejecutar la función al abrir cualquier terminal
      vim.cmd('autocmd! TermOpen term://* lua set_terminal_keymaps()')

    end,
  },
}

