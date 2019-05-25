package com.godson.kekbot.command.commands.meme;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.command.Command;
import com.godson.kekbot.command.CommandCategories;
import com.godson.kekbot.command.ImageCommand;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Trash extends ImageCommand {

    public Trash() {
        name = "trash";
        description = "This piece of trash was mistaken for art?";
        usage.add("magick <image URL>");
        usage.add("magick <attachment>");
        filename = "trash_but_its_not";
        category = CommandCategories.meme;
    }


    @Override
    protected byte[] generate(BufferedImage image) throws IOException {
        return null;
        //Unfinished command.



        /*ConvertCmd cmd = new ConvertCmd();
        IMOperation op = new IMOperation();

        op.addImage();
        op.distort()
        op.addImage("png:-");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Pipe pipeOut = new Pipe(null, stream);
        cmd.setOutputConsumer(pipeOut);
        if (KekBot.dev) cmd.setSearchPath("D:\\Program Files\\ImageMagick-6.9.9-Q16");

        cmd.run(op, image);

        stream.flush();
        byte[] finished = stream.toByteArray();
        stream.close();

        return finished;*/
    }
}
