#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D occlusion;

void main()
{
  vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
  float factor = texture2D(occlusion, inverted).r;
  gl_FragColor = texture2D(u_texture, inverted) * vec4(factor, factor, factor, 1.0);
}