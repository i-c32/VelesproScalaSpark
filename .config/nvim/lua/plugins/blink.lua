return {
  {
    'Saghen/blink.cmp',
    dependencies = 'rafamadriz/friendly-snippets',
    version = '*',

    opts = {
      keymap = { preset = 'default' },

      appearance = {
        use_nvim_cmp_as_default = true,
        nerd_font_variant = 'mono'
      },

      sources = {
        -- Lista de proveedores habilitados
        default = { 'lsp', 'path', 'snippets', 'buffer', 'codecompanion' },

        -- LOS PROVIDERS VAN DENTRO DE SOURCES
        providers = {
          codecompanion = {
            name = "CodeCompanion",
            module = "codecompanion.providers.completion.blink",
            enabled = true,
          },
        },
      },
    },
    opts_extend = { "sources.default" }
  },
}
