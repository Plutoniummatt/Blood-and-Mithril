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
  vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
  gl_FragColor = texture2D(u_texture, inverted) * color;
}