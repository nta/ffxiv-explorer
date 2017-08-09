package com.fragmenterworks.ffxivextract.helpers;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.models.Mesh;
import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.directx.DX9VertexElement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;

public class StudioModelWriter {
	
	public static void writeSmd(String path, Model model) throws IOException
	{		
		if (path.contains(".mdl"))
			path=path.replace(".mdl", ".smd");
		else if (!path.contains(".smd"))
			path+=".smd";

		for (int i = 0; i < model.getNumMesh(0); i++)
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(path.replace(".smd", "_"+i+".smd")));
		
			out.write("//FFXIV Model\r\n//Exported using FFXIV Explorer (modified version)\r\n\r\nversion 1\r\n");

			String boneData = model.getBoneData();

			if (boneData != null) {
				out.write(model.getBoneData());
			} else {
				out.write("nodes\r\n0 \"root\" -1\r\nend\r\n\r\n");
			}
				
			//out.write("mtllib " + path.replace(".obj", ".mtl").substring(path.lastIndexOf("\\")+1) + "\r\n");
			
			//out.write("usemtl mesh"+i+"\r\n");
			
			DX9VertexElement[] elements = model.getDX9Struct(0, i);
			DX9VertexElement vertElement = null, texCoordElement = null, normalElement = null, weightElement = null, indicesElement = null;
			
			for (DX9VertexElement e : elements)
			{
				switch (e.usage)
				{
				case 0:
					vertElement = e;
					break;
				case 1:
					weightElement = e;
					break;
				case 2:
					indicesElement = e;
					break;
				case 3:
					normalElement = e;
					break;
				case 4:
					texCoordElement = e;
					break;
				}
			}

			writeTriangles(vertElement, texCoordElement, normalElement, weightElement, indicesElement, model.getMeshes(0)[i], model, path.replace(".smd", "_" + i), out);
			
			out.close();
		}
				
		//writeMtl(path, model);
		
	}
	
	public static void writeMtl(String path, Model model) throws IOException
	{		
		if (path.contains(".obj"))
			path=path.replace(".obj", ".mtl");
		else if (!path.contains(".mtl"))
			path+=".mtl";
		
		BufferedWriter out = new BufferedWriter(new FileWriter(path));
		
		out.write("#FFXIV Material\r\n");
		
		for (int i = 0; i < model.getNumMesh(0); i++)
		{		
			out.write("newmtl mesh" + i + "\r\n");
			out.write("illum 2\r\n");
			out.write("Ka 0.9882 0.9882 0.9882\r\n");
			out.write("Kd 0.9882 0.9882 0.9882\r\n");
			out.write("Ks 0.0000 0.0000 0.0000\r\n");			
			out.write("map_Kd "+ path.replace(".mtl", "_d.tga").substring(path.lastIndexOf("\\")+1) +"\r\n");			
			out.write("map_bump "+ path.replace(".mtl", "_n.tga").substring(path.lastIndexOf("\\")+1) +"\r\n");
			out.write("\r\n");
		}
		
		out.close();
	}

	private static ExportVertex readVert(int i,
										 DX9VertexElement vertElement, DX9VertexElement texCoordElement, DX9VertexElement normalElement,
										 DX9VertexElement weightElement, DX9VertexElement indicesElement, Mesh mesh, Model model) {
		ByteBuffer vertBuffer = mesh.vertBuffers[vertElement.stream];
		vertBuffer.order(ByteOrder.LITTLE_ENDIAN);

		// read position
		float x = 0.0f, y = 0.0f, z = 0.0f;

		vertBuffer.position((i * mesh.vertexSizes[vertElement.stream]) + vertElement.offset);

		if (vertElement.datatype == 13 || vertElement.datatype == 14) {
			x = Utils.convertHalfToFloat(vertBuffer.getShort());
			y = Utils.convertHalfToFloat(vertBuffer.getShort());
			z = Utils.convertHalfToFloat(vertBuffer.getShort());
		} else if (vertElement.datatype == 2) {
			x = vertBuffer.getFloat();
			y = vertBuffer.getFloat();
			z = vertBuffer.getFloat();
		}

		// read texcoord
		float u, v;

		vertBuffer = mesh.vertBuffers[texCoordElement.stream];
		vertBuffer.order(ByteOrder.LITTLE_ENDIAN);

		vertBuffer.position((i*mesh.vertexSizes[texCoordElement.stream]) + texCoordElement.offset);
		u = Utils.convertHalfToFloat(vertBuffer.getShort());
		v = Utils.convertHalfToFloat(vertBuffer.getShort());

		// read normal
		float nx, ny, nz;

		vertBuffer = mesh.vertBuffers[normalElement.stream];
		vertBuffer.order(ByteOrder.LITTLE_ENDIAN);

		vertBuffer.position((i*mesh.vertexSizes[normalElement.stream]) + normalElement.offset);

		nx = Utils.convertHalfToFloat(vertBuffer.getShort());
		ny = Utils.convertHalfToFloat(vertBuffer.getShort());
		nz = Utils.convertHalfToFloat(vertBuffer.getShort());

		float[] weights = new float[4];
		int[] indices = new int[4];

		if (weightElement != null && indicesElement != null) {
			// read weights
			vertBuffer = mesh.vertBuffers[weightElement.stream];
			vertBuffer.order(ByteOrder.LITTLE_ENDIAN);

			vertBuffer.position((i * mesh.vertexSizes[weightElement.stream]) + weightElement.offset);

			for (int j = 0; j < 4; j++) {
				weights[j] = (vertBuffer.get() & 0xFF) / 255.0f;
			}

			// read indices
			vertBuffer = mesh.vertBuffers[indicesElement.stream];
			vertBuffer.order(ByteOrder.LITTLE_ENDIAN);

			vertBuffer.position((i * mesh.vertexSizes[indicesElement.stream]) + indicesElement.offset);

			for (int j = 0; j < 4; j++) {
				indices[j] = model.mapBone(vertBuffer.get() & 0xFF, mesh);
			}
		}

		return new ExportVertex(x, y, z, u, v, nx, ny, nz, weights, indices);
	}

	private static void writeVert(ExportVertex vertex, BufferedWriter out) throws IOException {
		out.write(String.format("0    %f %f %f    %f %f %f    %f %f ",
				vertex.getX(), vertex.getY(), vertex.getZ(),
				vertex.getNx(), vertex.getNy(), vertex.getNz(),
				1.0 - vertex.getU(), vertex.getV()));

		if (vertex.getWeights() != null) {
			out.write(String.format("%d ", vertex.getWeights().length));

			for (int i = 0; i < vertex.getWeights().length; i++) {
				out.write(String.format("%d %f ", vertex.getIndices()[i], vertex.getWeights()[i]));
			}
		}

		out.write("\r\n");
	}

	private static void writeTriangles(DX9VertexElement vertElement, DX9VertexElement texCoordElement, DX9VertexElement normalElement,
									   DX9VertexElement weightElement, DX9VertexElement indicesElement, Mesh mesh, Model model, String path, BufferedWriter out) throws IOException {
		ByteBuffer indexBuffer = mesh.indexBuffer;
		indexBuffer.position(0);
		indexBuffer.order(ByteOrder.LITTLE_ENDIAN);

		out.write("triangles\r\n");

		for (int i = 0; i < mesh.numIndex; i += 3) {
			int ind1 = indexBuffer.getShort();
			int ind2 = indexBuffer.getShort();
			int ind3 = indexBuffer.getShort();

			ExportVertex v1 = readVert(ind1, vertElement, texCoordElement, normalElement, weightElement, indicesElement, mesh, model);
			ExportVertex v2 = readVert(ind2, vertElement, texCoordElement, normalElement, weightElement, indicesElement, mesh, model);
			ExportVertex v3 = readVert(ind3, vertElement, texCoordElement, normalElement, weightElement, indicesElement, mesh, model);

			out.write(Paths.get(path).getFileName().toString());

			out.write("\r\n");

			writeVert(v1, out);
			writeVert(v2, out);
			writeVert(v3, out);
		}

		out.write("end\r\n");
	}
}
