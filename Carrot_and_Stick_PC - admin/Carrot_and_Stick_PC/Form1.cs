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
using Firebase.Database;

namespace Carrot_and_Stick_PC
{
    public partial class Carrot1 : Form
    {
        public Carrot1()
        {
            InitializeComponent();

            this.ShowInTaskbar = false;
            this.notifyIcon1.Visible = true;
            notifyIcon1.ContextMenuStrip = contextMenuStrip1;

            webBrowser1.Navigate("https://carotandstick-35d0e.firebaseapp.com/");
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
        ////////////////////////////////////////////


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
        /////////////////////////////////////////////////////////


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
        public const int WM_COMMAND = 0x111;
        public const int MIN_ALL = 419;
        public const int MIN_ALL_UNDO = 416;

        public void checkAlert(object state)
        {
            //Console.WriteLine("checkAlert");

            int window = FindWindow(null, "Task Manager");
            if (window != 0)
            {
                //Console.WriteLine("Task Manager");
                SendMessage((int)window, WM_SYSCOMMAND, SC_CLOSE, 0);
            }
            else
            {
                //Console.WriteLine("Nope");
            }
            window = FindWindow(null, "작업 관리자");
            if (window != 0)
            {
                //Console.WriteLine("Task Manager");
                SendMessage((int)window, WM_SYSCOMMAND, SC_CLOSE, 0);
            }
            else
            {
                //Console.WriteLine("Nope");
            }
        }

        public System.Threading.Timer timer_closer;
        /* Code to Close error message from taskmanager Ends Here */
        ////////////////////////////////////////////////////////////


        public void activate_window_fullscreen()
        {
            /*
            방법 1
            this.StartPosition = FormStartPosition.Manual;

            Rectangle fullScreen_bounds = Rectangle.Empty;

            foreach (var screen in Screen.AllScreens)
            {
                fullScreen_bounds = Rectangle.Union(fullScreen_bounds, screen.Bounds);
            }
            this.ClientSize = new Size(fullScreen_bounds.Width, fullScreen_bounds.Height);
            this.Location = new Point(fullScreen_bounds.Left, fullScreen_bounds.Top);
            */

            /*
            방법 2
            int screenLeft = SystemInformation.VirtualScreen.Left;
            int screenTop = SystemInformation.VirtualScreen.Top;
            int screenWidth = SystemInformation.VirtualScreen.Width;
            int screenHeight = SystemInformation.VirtualScreen.Height;

            this.Size = new System.Drawing.Size(screenWidth, screenHeight);
            this.Location = new System.Drawing.Point(screenLeft, screenTop);
            */

            /*
             * 주 모니터에 띄우고, 나머지 모니터는 걍 가리기
             */
            this.WindowState = FormWindowState.Maximized;

            Screen[] screens = Screen.AllScreens;
            int order = 0;
            foreach (Screen now_screen in screens)
            {
                order++;
                if (order == 1)
                {
                    continue;
                }

                Console.WriteLine(now_screen.WorkingArea.Location + " | " + now_screen.WorkingArea.Size);

                Form aForm = new Form();

                aForm.Name = "Carrot" + order.ToString();
                Label temp_label = new Label() { Text = order.ToString() };
                temp_label.Size = new System.Drawing.Size(200, 200);
                aForm.Controls.Add(temp_label);
                temp_label.Font = new Font("나눔고딕 ExtraBold", 100, FontStyle.Bold);
                aForm.FormBorderStyle = FormBorderStyle.None;
                aForm.Show();  // Or just use Show(); if you don't want it to be modal.
                aForm.Location = new System.Drawing.Point(now_screen.WorkingArea.Location.X, now_screen.WorkingArea.Location.Y);
                //aForm.Size = new System.Drawing.Size(now_screen.WorkingArea.Size.Width, now_screen.WorkingArea.Size.Height);
                aForm.WindowState = FormWindowState.Maximized;
                aForm.BringToFront();

                aForm.TopMost = true;
            }

            this.Activate();
            this.Focus();
            this.BringToFront();
            this.TopMost = true;

            textBox1.Focus();

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

        private void button10_Click(object sender, EventArgs e)
        {
            var firebase = new FirebaseClient("https://carotandstick-35d0e.firebaseio.com/");
            //Console.WriteLine(firebase.ToString());
            Console.WriteLine("button!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!()");
        }
    }
}
