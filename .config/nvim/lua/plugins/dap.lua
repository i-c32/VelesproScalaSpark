return {
  "mfussenegger/nvim-dap",
  dependencies = {
    "rcarriga/nvim-dap-ui",
    "mfussenegger/nvim-dap-python",
    "nvim-neotest/nvim-nio", -- Requerido por dap-ui
  },
  keys = {
    { "<F5>", function() require('dap').continue() end, desc = "Debug: Continue" },
    { "<leader>db", function() require('dap').toggle_breakpoint() end, desc = "Debug: Toggle Breakpoint" },
    { "<leader>du", function() require('dapui').toggle() end, desc = "Debug: Toggle UI" },
  },
  config = function()
    local dap = require("dap")
    local dapui = require("dapui")
    local dappython = require("dap-python")

    dapui.setup()
    
    -- 3. Configuración del adaptador de Python
    -- Indica la ruta a tu ejecutable de python
    dappython.setup("python") 

    -- Abrir y cerrar la UI automáticamente
    dap.listeners.after.event_initialized["dapui_config"] = function()
      dapui.open()
    end
    dap.listeners.before.event_terminated["dapui_config"] = function()
      dapui.close()
    end
    dap.listeners.before.event_exited["dapui_config"] = function()
      dapui.close()
    end
  end
}
