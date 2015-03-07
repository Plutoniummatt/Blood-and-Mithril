#ifdef GL_ES
#define LOWP lowp
precision highp float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec4 color;

void main()
{
  gl_FragColor = texture2D(u_texture, v_texCoords) * color;
}