#version 120

#import inputs

void main() {
    float Pi = 6.28318530718; // Pi*2
    
    float Directions = 16.0;
    float Quality = 4.0;
    float Size = 8.0;
    vec2 res = vec2(windowX, windowY);
    vec2 Radius = Size/res;

    vec2 uv = gl_TexCoord/res;
    
    vec4 Color = texture(bgl_RenderedTexture, uv);

    for( float d=0.0; d<Pi; d+=Pi/Directions)
    {
        for(float i=1.0/Quality; i<=1.0; i+=1.0/Quality)
        {
            Color += texture( bgl_RenderedTexture, uv+vec2(2.*cos(d),2.*sin(d))*Radius*i);      
        }
    }

    Color /= Quality * Directions - 15.0;
    gl_FragColor =  Color;
}