package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) {
		Model result = new Model();
		Scanner scanner = new Scanner(fileContent);

		int lineInd = 0;

		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+"))); // сплитим по пробелам
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
			switch (token) {
				// Для структур типа вершин методы написаны так, чтобы ничего не знать о внешней среде.
				// Они принимают только то, что им нужно для работы, а возвращают только то, что могут создать.
				// Исключение - индекс строки. Он прокидывается, чтобы выводить сообщение об ошибке.
				// Могло быть иначе. Например, метод parseVertex мог вместо возвращения вершины принимать вектор вершин
				// модели или сам класс модели, работать с ним.
				// Но такой подход может привести к большему количеству ошибок в коде. Например, в нем что-то может
				// тайно сделаться с классом модели.
				// А еще это портит читаемость
				// И не стоит забывать про тесты. Чем проще вам задать данные для теста, проверить, что метод рабочий,
				// тем лучше.
				case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
				case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
				case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
				case OBJ_FACE_TOKEN -> result.polygons.add(parseFace(wordsInLine, lineInd, result.vertices.size()));
				default -> {}
			}
		}
		checkInfoAfterReading(result);
		return result;
	}

	// Всем методам кроме основного я поставил модификатор доступа protected, чтобы обращаться к ним в тестах
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));
		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));
		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));
		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd, int verticesAmount) {
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd, verticesAmount);
		}

		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		result.setNormalIndices(onePolygonNormalIndices);
		return result;
	}

	// Обратите внимание, что для чтения полигонов я выделил еще один вспомогательный метод.
	// Это бывает очень полезно и с точки зрения структурирования алгоритма в голове, и с точки зрения тестирования.
	// В радикальных случаях не бойтесь выносить в отдельные методы и тестировать код из одной-двух строчек.
	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd,
			int verticesAmount) {
		try {
			String[] wordIndices = wordInLine.split("/");
			// проверяем как у нас указаны вершины: используется обычная адресация или отрицательная, если обычная, то процесс обычный,
			// если отрицательная, то к отрицательному индексу прибавляем кол-во вершин (в obj-файле принято использоваться одинаковый тип индексации,
			// если вершины заданы отрицательной индексацией, то текстурные вершины и нормали, тоже должны быть заданы отрицательной адресацией, поэтому
			// достаточно проверить тип адресации только у вершин)
			boolean flag = Integer.parseInt(wordIndices[0]) < 0;
			switch (wordIndices.length) {
				case 1 -> {
					if (flag) {
						onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) + verticesAmount);
					} else {
						onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					}
				}
				case 2 -> {
					if (flag) {
						onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) + verticesAmount);
						onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) + verticesAmount);
					} else {
						onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
						onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
					}
				}
				case 3 -> {
					if (flag) {
						onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) + verticesAmount);
						onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]) + verticesAmount);
						if (!wordIndices[1].equals("")) {
							onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) + verticesAmount);
						}
					} else {
						onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
						onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
						if (!wordIndices[1].equals("")) {
							onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
						}
					}
				}
				default -> {
					throw new ObjReaderException("Invalid element size.", lineInd);
				}
			}
		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);
		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd);
		}
	}

	// номера вершин, текстурных вершин и нормалей начинаются с единицы
	// нужны геттеры в vector2f и vector3f
	protected static void checkInfoAfterReading(Model model) {
		ArrayList<Vector3f> modelVertices = model.vertices;
		ArrayList<Vector2f> modelTextureVertices = model.textureVertices;
		ArrayList<Vector3f> modelNormals = model.normals;
		ArrayList<Polygon> modelPolygons = model.polygons;
		Set<Float> polygonVerticesXsSet = new HashSet<>();
		Set<Float> polygonVerticesYsSet = new HashSet<>();
		Set<Float> polygonVerticesZsSet = new HashSet<>();
		for (int i = 0; i < modelPolygons.size(); i++) {
			Polygon polygon = model.polygons.get(i);
			ArrayList<Integer> polygonVertices = polygon.getVertexIndices();
			ArrayList<Integer> polygonTextureVertices = polygon.getTextureVertexIndices();
			ArrayList<Integer> polygonNormals = polygon.getNormalIndices();
			if (modelVertices.size() < 3) {
				throw new RuntimeException("Model has 2 or fewer vertices."); // в моделе две вершины или меньше
			} else if (modelPolygons.size() == 0) {
				throw new RuntimeException("There is not a single polygon in the model."); // в моделе нет ни одного полигона
			} else if (polygonVertices.size() != polygonTextureVertices.size() && (polygonTextureVertices.size() != 0)) {
				throw new RuntimeException("Not all vertices of polygon number " + i + " have a texture vertex attached to them"); // не ко всем вершинам полигона номер i привязана текстурная вершина
			} else if (polygonVertices.size() != polygonNormals.size() && (polygonNormals.size() != 0)) {
				throw new RuntimeException("Not all vertices of polygon number " + i + " have a normals attached to them"); // не ко всем вершинам полигона номер i привяза нормаль
			}
			Vector3f vertices;
			for (int j = 0; j < polygonVertices.size(); j++) {
				try {
					vertices = modelVertices.get(polygonVertices.get(j));
					polygonVerticesXsSet.add(vertices.getX());
					polygonVerticesYsSet.add(vertices.getY());
					polygonVerticesZsSet.add(vertices.getZ());
					/**
					здесь я должен проверять вершины полигона, отдельно по каждой координате. Допустим, добавлять x,y,z в множество (set), в идеале только
					одно множество должно быть длины 1, то есть в котором один элемент. Это будет означать, что все точки полигона лежат в одной плоскости, если
					 же два множества будут иметь длину 1, то это значит, что точки полигона лежат на одной кривой, если все 3 множества будут длины 1, то
					 точки полигоны равны
					*/
				} catch (IndexOutOfBoundsException exception) {
					throw new RuntimeException("For polygon #" + (i+1) + ": there is no vertex with number " + (polygonVertices.get(j)+1) + " in the file."); // в файле нет вершины с номером j
				}
			}
			int setsLengthCounter = ObjReader.counterForSetsLength(polygonVerticesXsSet, polygonVerticesYsSet, polygonVerticesZsSet);
			if (setsLengthCounter == 0) {
				throw new RuntimeException("For polygon #" + (i+1) + ": it's points do not lie in the same plane");
			} else if (setsLengthCounter > 1) {
				throw new RuntimeException("For polygon #" + (i+1) + ": all points of it lie on a straight line or it's represented by only one point");
			}
			for (int k = 0; k < polygonTextureVertices.size(); k++) {
				try {
					/**
					 * мб проверять значение текстурных координат, чтобы они лежали на отрезке [0;1]
					 */
					Vector2f textureVertices = modelTextureVertices.get(polygonTextureVertices.get(k));
				} catch (IndexOutOfBoundsException exception) {
					throw new RuntimeException("For polygon #" + (i+1) +": there is no texture vertex with number " + (polygonTextureVertices.get(k)+1) + " in the file."); // в файле нет текстурной вершины с номером k
				}
			}
			for (int l = 0; l < polygonNormals.size(); l++) {
				try {
					/**
					 * мб проверять значение нормалей, чтобы они лежали на отрезке [0;1]
					 */
					Vector3f normals = modelNormals.get(polygonNormals.get(l));
				} catch (IndexOutOfBoundsException exception) {
					throw new RuntimeException("For polygon #" + (i+1) + ": there is no normal with number " + (polygonNormals.get(l)+1) + " in the file."); // в файле нет текстурной вершины с номером l
				}
			}
		}
	}

	protected static int counterForSetsLength(Set<Float>... hashSets) {
		int counter = 0;
		for (Set<Float> hashSet : hashSets) {
			if (hashSet.size() == 1) {
				counter++;
			}
		}
		return counter;
	}
}
