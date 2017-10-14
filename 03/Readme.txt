Krishna V Jayanthi
Arnold Esguerra
5310 Computer Graphics
Fall 2017
Assignment 3

=========================================

1.  The scene graph is made all of the objects that is being rendered to
the viewer and is organized in a tree-like structure.  It obtains the data for the structure
by the View class using the sgraph package to take in the XML file from
scenegraphmodels and parsing it appropriately into its respective PolygonMeshes.

This parsing is done in the SceneXMLReader class (MyHandler and SceneXMLReader nested classes).  During
parsing, elements are pushed onto a stack, matching how they are nested in the xml file.
The elements that get pushed are LeafNodes.  These leaves are parsed by matching the name from the xml
file to the .obj files in the models package. They are used to obtain the appropriate Polygon meshses using
ObjImporter and ultimately added ot the scenegraph.  When adding to the scenegraph, a map is used to match
the name to the mesh for efficient lookup.

Additionally, the transforms are also parsed into their own separate nodes, appropriately named TransformNode.
These transformnodes are the result of parsing the xml files for the type of transformation (scale, rotate,
translate), as well as their values to save the respective matrix transformation.

More plainly put, the scenegraph contains the nodes for both the objects, as well as separate nodes of transforms that will be applied to the subtrees within the overall scenegraph. This allows transforms to be applied to the ever-evolving perception of what we consider an object (e.g. a fan, a spinning fan, an oscillating fan, etc. as seen in class).

=========================================

2. The scene graph is drawn by first being initialized in the View class, which sets off the process
of parsing the xml file as described in #1.  The view class also initializes the renderer
and passes it to the scenegraph.  When the view class calls the draw method, it is passed
to the scene graph, who passes it to the renderer class (GL3ScenegraphRenderer).  As stated
in the end of #1, by placing the transformnodes in the appropriate place in the scenegraph, the transformations
are applied to all of the subgraphs/subtrees below that node.  In the XML file, we are structuring
the transformations next to the object/composite objects that we can the transformation applied to (with the
<set> tags. The same idea is applied when building the scenegraph to make sure the transofmrations are
applied correctly.

=========================================

3. The GL3SceneGraphRenderer class's job is ultimately to render every mesh in the scenegraph, as
implied by the name.  It contains the shader program and to further decouple the structure of the code.
In previous assignments, this was all done in the View class.  If you wanted to create a textual rendering
of the scene, you would use the MyTextRenderer ......