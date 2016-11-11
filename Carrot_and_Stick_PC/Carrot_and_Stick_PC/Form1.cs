using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Microsoft.Win32;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.Threading;

namespace Carrot_and_Stick_PC
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();

            this.ShowInTaskbar = false;
            this.notifyIcon1.Visible = true;
            notifyIcon1.ContextMenuStrip = contextMenuStrip1;
        }

        //////////////////////////////////////////////
        /* Code to Disable Ctrl+Alt+Del Starts Here */
        public void KillCtrlAltDelete()
        {
            RegistryKey regkey = default(RegistryKey);
            string keyValueInt = "1";
            string subKey = "Software\\Microsoft\\Windows\\CurrentVersion\\Policies\\System";
            try
            {
                regkey = Registry.CurrentUser.CreateSubKey(subKey);
                regkey.SetValue("DisableTaskMgr", keyValueInt);
                regkey.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
            }
        }
        public static void EnableCTRLALTDEL()
        {
            try
            {
                string subKey = @"Software\\Microsoft\\Windows\\CurrentVersion\\Policies\\System";
                RegistryKey rk = Registry.CurrentUser;
                RegistryKey sk1 = rk.OpenSubKey(subKey);
                if (sk1 != null)
                {
                    rk.DeleteSubKeyTree(subKey);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
            }
        }
        /* Code to Disable Ctrl+Alt+Del Ends Here */
        //////////////////////////////////////////////


        ///////////////////////////////////////////////////////////
        /* Code to Disable WinKey, Alt+Tab, Ctrl+Esc Starts Here */

        // Structure contain information about low-level keyboard input event 
        [StructLayout(LayoutKind.Sequential)]
        private struct KBDLLHOOKSTRUCT
        {
            public Keys key;
            public int scanCode;
            public int flags;
            public int time;
            public IntPtr extra;
        }
        //System level functions to be used for hook and unhook keyboard input  
        private delegate IntPtr LowLevelKeyboardProc(int nCode, IntPtr wParam, IntPtr lParam);
        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern IntPtr SetWindowsHookEx(int id, LowLevelKeyboardProc callback, IntPtr hMod, uint dwThreadId);
        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern bool UnhookWindowsHookEx(IntPtr hook);
        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern IntPtr CallNextHookEx(IntPtr hook, int nCode, IntPtr wp, IntPtr lp);
        [DllImport("kernel32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern IntPtr GetModuleHandle(string name);
        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        private static extern short GetAsyncKeyState(Keys key);
        //Declaring Global objects     
        private IntPtr ptrHook;
        private LowLevelKeyboardProc objKeyboardProcess;

        private IntPtr captureKey(int nCode, IntPtr wp, IntPtr lp)
        {
            if (nCode >= 0)
            {
                KBDLLHOOKSTRUCT objKeyInfo = (KBDLLHOOKSTRUCT)Marshal.PtrToStructure(lp, typeof(KBDLLHOOKSTRUCT));

                // Disabling Windows keys 

                if (objKeyInfo.key == Keys.RWin || objKeyInfo.key == Keys.LWin || objKeyInfo.key == Keys.Tab && HasAltModifier(objKeyInfo.flags) || objKeyInfo.key == Keys.F4 && HasAltModifier(objKeyInfo.flags) || objKeyInfo.key == Keys.Escape && (ModifierKeys & Keys.Control) == Keys.Control)
                {
                    return (IntPtr)1; // if 0 is returned then All the above keys will be enabled
                }
            }
            return CallNextHookEx(ptrHook, nCode, wp, lp);
        }

        bool HasAltModifier(int flags)
        {
            return (flags & 0x20) == 0x20;
        }

        /* Code to Disable WinKey, Alt+Tab, Ctrl+Esc Ends Here */
        ///////////////////////////////////////////////////////////


        //////////////////////////////////////////////
        /* Code to Disable TaskBar Menu Starts Here */
        [DllImport("user32.dll")]
        private static extern int FindWindow(string className, string windowText);
        [DllImport("user32.dll")]
        private static extern int ShowWindow(int hwnd, int command);

        private const int SW_HIDE = 0;
        private const int SW_SHOW = 1;

        public static void ShowStartMenu()
        {
            int hwnd = FindWindow("Shell_TrayWnd", "");
            ShowWindow(hwnd, SW_SHOW);
        }
        public static void HideStartMenu()
        {
            int hwnd = FindWindow("Shell_TrayWnd", "");
            ShowWindow(hwnd, SW_HIDE);
        }
        /* Code to Disable TaskBar Menu Ends Here */
        ////////////////////////////////////////////


        //////////////////////////////////////////////
        /* Code to Close error message from taskmanager Starts Here */
        [DllImport("user32.dll")]
        public static extern int SendMessage(int hWnd, uint Msg, int wParam, int lParam);
        public const int WM_SYSCOMMAND = 0x0112;
        public const int SC_CLOSE = 0xF060;

        public void checkAlert(object state)
        {
            Console.WriteLine("checkAlert");

            int window = FindWindow(null, "Task Manager1");
            if (window != 0)
            {
                Console.WriteLine("Task Manager");
                SendMessage((int)window, WM_SYSCOMMAND, SC_CLOSE, 0);
            } else
            {
                Console.WriteLine("Nope");
            }
        }

        public System.Threading.Timer timer_closer;
        /* Code to Close error message from taskmanager Ends Here */
        ////////////////////////////////////////////////////////////


        public void activate_window_fullscreen()
        {
            this.WindowState = FormWindowState.Maximized;
        }
        public void activate_keboard_hooking()
        {
            ProcessModule objCurrentModule = Process.GetCurrentProcess().MainModule;
            objKeyboardProcess = new LowLevelKeyboardProc(captureKey);
            ptrHook = SetWindowsHookEx(13, objKeyboardProcess, GetModuleHandle(objCurrentModule.ModuleName), 0);
        }
        public void activate_alert_closer()
        {
            timer_closer = new System.Threading.Timer(checkAlert);
            timer_closer.Change(0, 100);
        }
        public void deactivate_keboard_hooking()
        {
            UnhookWindowsHookEx(ptrHook);
        }
        public void deactivate_alert_closer()
        {
            timer_closer.Dispose();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            activate_window_fullscreen();
            KillCtrlAltDelete();
            HideStartMenu();
            activate_keboard_hooking();
            activate_alert_closer();
        }
        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            EnableCTRLALTDEL();
            ShowStartMenu();
            deactivate_keboard_hooking();
            deactivate_alert_closer();
        }

    
        private void button1_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void button2_Click(object sender, EventArgs e)
        {
            activate_keboard_hooking();
        }

        private void button3_Click(object sender, EventArgs e)
        {
            deactivate_keboard_hooking();
        }

        private void button4_Click(object sender, EventArgs e)
        {
            HideStartMenu();
        }

        private void button5_Click(object sender, EventArgs e)
        {
            ShowStartMenu();
        }

        private void button7_Click(object sender, EventArgs e)
        {
            KillCtrlAltDelete();
        }

        private void button8_Click(object sender, EventArgs e)
        {
            EnableCTRLALTDEL();
        }

        private void button6_Click(object sender, EventArgs e)
        {
            activate_alert_closer();
        }

        private void button9_Click(object sender, EventArgs e)
        {
            deactivate_alert_closer();
        }
    }
}
