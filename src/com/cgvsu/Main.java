package com.cgvsu;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        Path fileName = Path.of("3DModels/SimpleModelsForReaderTests/GGGG.obj");
        Path fileName2 = Path.of("3DModels/SimpleModelsForReaderTests/Test02.obj");
        Path fileName3 = Path.of("3DModels/SimpleModelsForReaderTests/Test03.obj");
        Path fileName4 = Path.of("3DModels/SimpleModelsForReaderTests/Test04.obj");
        Path fileName5 = Path.of("3DModels/SimpleModelsForReaderTests/Test05.obj");
        Path fileName6 = Path.of("3DModels/SimpleModelsForReaderTests/Test06.obj");
        Path fileName7 = Path.of("3DModels/SimpleModelsForReaderTests/Test07.obj");
        Path teapotInvalidVertexCount = Path.of("3DModels/SimpleModelsForReaderTests/TeapotInvalidVertexCount.obj");
        Path teapot = Path.of("3DModels/SimpleModelsForReaderTests/Teapot.obj");
        Path body = Path.of("3DModels/Faceform/WrapBody.obj");
        Path caracal_cube = Path.of("3DModels/CaracalCube/caracal_cube.obj");
        String fileContent = null;
        try {
            fileContent = Files.readString(teapot);
        } catch (MalformedInputException exception) {
            System.out.println("Ошибка: в названии файла использована кириллица.");
            System.exit(1);
        } catch (NoSuchFileException exception) {
            System.out.println("Ошибка: указанный файл не найден.");
            System.exit(1);
        }

        System.out.println("Loading model ...");
        Model model = ObjReader.read(fileContent);

        System.out.println("Vertices: " + /*model.vertices.size() +*/ model.vertices);
        System.out.println("Texture vertices: " + /*model.textureVertices.size() +*/ model.textureVertices);
        System.out.println("Normals: " + /*model.normals.size() +*/ model.normals);
        System.out.println("Polygons: " + /*model.polygons.size() +*/ model.polygons);
    }
}
