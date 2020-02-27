using System;
using System.IO;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using DSharpPlus;
using DSharpPlus.CommandsNext;
using ImageMagick;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;

namespace KekBot {
    class Program {

        static DiscordClient discord;
        static CommandsNextModule commands;


        static void Main(string[] args) {
            MainAsync(args).ConfigureAwait(false).GetAwaiter().GetResult();
        }

        static async Task MainAsync(string[] args) {
            discord = new DiscordClient(new DiscordConfiguration {
                Token = "NDA2NTgzNzA2Njc4MDY3MjAw.XlWoTA.koxFZFM30PN0ua9YOjbDHM8_4-s",
                TokenType = TokenType.Bot,
                LogLevel = LogLevel.Debug,
                UseInternalLogHandler = true
            });

            commands = discord.UseCommandsNext(new CommandsNextConfiguration {
                StringPrefix = "$",
                EnableMentionPrefix = true
            });

            await discord.ConnectAsync();
            await Task.Delay(-1);
        }
    }
}
