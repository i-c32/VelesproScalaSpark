return {
  'lewis6991/gitsigns.nvim',
  event = { "BufReadPost", "BufNewFile" },
  config = function()
    -- Forzar que la columna de signos siempre esté visible
    vim.opt.signcolumn = "yes"

    require('gitsigns').setup({
      debug_mode = true,
      attach_to_untracked = true,
      _git_version = 'auto',
      git_main_alias = 'main',  -- o el nombre de tu rama principal
      worktrees = {
        {
          toplevel = vim.env.HOME .. '/workspace',
          gitdir = vim.env.HOME .. '/workspace/.git',
        },
      },
      current_line_blame = false,

      on_attach = function(bufnr)
        local gs = package.loaded.gitsigns
        local function map(mode, l, r, opts)
          opts = opts or {}
          opts.buffer = bufnr
          vim.keymap.set(mode, l, r, opts)
        end

        map('n', ']c', function()
          if vim.wo.diff then return ']c' end
          vim.schedule(function() gs.next_hunk() end)
          return '<Ignore>'
        end, { expr = true, desc = "Siguiente cambio" })

        map('n', '[c', function()
          if vim.wo.diff then return '[c' end
          vim.schedule(function() gs.prev_hunk() end)
          return '<Ignore>'
        end, { expr = true, desc = "Cambio anterior" })

        map('n', '<leader>hs', gs.stage_hunk,                          { desc = "Stage Hunk" })
        map('n', '<leader>hr', gs.reset_hunk,                          { desc = "Reset Hunk" })
        map('n', '<leader>hp', gs.preview_hunk,                        { desc = "Previsualizar cambio" })
        map('n', '<leader>hb', function() gs.blame_line{ full=true } end, { desc = "Blame completo" })
        map('n', '<leader>hd', gs.diffthis,                            { desc = "Ver Diff" })
      end
    })
  end
}
